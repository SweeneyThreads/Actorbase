package server

import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.dispatch.ExecutionContexts._
import akka.event.{Logging, LoggingAdapter}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import server.actors.{Doorkeeper, MapManager}
import server.enums.EnumPermission
import server.enums.EnumPermission.UserPermission
import server.utils.ConfigurationManager

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by matteobortolazzo on 02/05/2016.
  */



/**
  * The Actorbase server, contains the main method of the server-side application of Actorbase.
  */
object Server {
  var log:LoggingAdapter = null

  var clusterListener: ActorRef = null
  var sFclusterListener: ActorRef = null
  var sKclusterListener: ActorRef = null

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  def main(args: Array[String]) {
    val conf = ConfigFactory.load()
    val system = ActorSystem("System", conf)
    log = Logging.getLogger(system, this)

    clusterListener= system.actorOf(Props[ClusterListener])
    sFclusterListener= system.actorOf(Props[ClusterListener])
    sKclusterListener= system.actorOf(Props[ClusterListener])

    loadDatabases(system)
    createDoorkeepers(system)
    log.info("Server started")
  }

  /**
    * Loads all the databases.
    *
    * @param system The actor system.
    */
  private def loadDatabases(system: ActorSystem): Unit = {
    system.actorOf(Props(new MapManager("test")), name="test")
    system.actorOf(Props(new MapManager("master")), name="master")
    log.info("Databases loaded")
  }

  /**
    * Reads the doorkeeper's configuration file and creates the Doorkeeper
    *
    * @param system The actor system.
    */
  private def createDoorkeepers(system: ActorSystem): Unit ={
    val confManager = new ConfigurationManager
    try {
      val accesses = confManager.readDoorkeepersSettings("conf/accesses.json")
      for (address <- accesses.keySet()) {
        val port = accesses.get(address)
        system.actorOf(Props(classOf[Doorkeeper], port))
      }
    }
    catch {
      case e:Exception => println("File not found")
    }
  }
}

/* RICETTA PER LA FELICITA'

* Fai partire il server (Debug).
* Fai partire il client (Run).
* Scrivi 'connect localhost:8181 admin admin'
* Divertiti con i comandi che Borto ha implementato per te!
*
* listdb, selectdb nome, createdb nome, deletedb nome,
* listmap, selectmap nome, createmap nome, deletemap nome,
* insert 'key' value, update 'key' value, remove 'key', find 'key', keys.
*
* Quando vuoi disconnetterti scrivi 'disconnect'
*
* Il server per ora NON risponde al client MA viene tutto stampato su console.
*
* */

/*  COME è STRUTTURAT0 IL SERVER
*
* Il server crea un Doorkeeper (porta 8181) e uno Storemanager ("test"). **uno storemanager rappresenta una database**
* Il Doorkeeper, per ogni connessione, crea un Usermanager.
*
* Ogni stringa inviata dal un client verrà gestita dal proprio Usermanager.
* Quest'ultimo ha il compito di creare i messaggi (con l'aiuto di una classe di parsing),
* controllare i permessi e spedire ad un Main.
*
* Un Main gestisce i messaggi di un client.
* L'attore Main ha la responsabilità di gestire i database (Storemanager).
* L'attore Storemanager ha la responsabilità di gestire le mappe (Storefinder)*
* Storefinder e Storekeeper li conosciamo tutti.
*
* Ho implementato l'indicizzazione degli Storekeeper attraverso espressioni regolari.
*
* */