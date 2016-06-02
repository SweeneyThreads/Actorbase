package server

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.dispatch.ExecutionContexts._
import akka.event.{Logging, LoggingAdapter}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import server.actors.{Doorkeeper, Storemanager}
import server.enums.EnumPermission
import server.enums.EnumPermission.UserPermission
import server.utils.FileReader

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by matteobortolazzo on 02/05/2016.
  */



/**
  * The Actorbase server, contains the main method of the server-side application of Actorbase.
  */
object Server {
  var system:ActorSystem = null
  var log:LoggingAdapter = null
  var fileReader:FileReader = null

  var storemanagers: ConcurrentHashMap[String, ActorRef] = null
  var users: ConcurrentHashMap[String, String] = null
  var permissions: ConcurrentHashMap[String, ConcurrentHashMap[String, UserPermission]] = null

  var clusterListener: ActorRef = null
  var sFclusterListener: ActorRef = null
  var sKclusterListener: ActorRef = null

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  def main(args: Array[String]) {
    val conf = ConfigFactory.load()
    system = ActorSystem("System", conf)
    log = Logging.getLogger(system, this)

    clusterListener = system.actorOf(Props[ClusterListener])
    sFclusterListener = system.actorOf(Props[ClusterListener])
    sKclusterListener = system.actorOf(Props[ClusterListener])


    loadUsers()
    loadUsersPermissions()
    loadDatabases(system)
    system.actorOf(Props(classOf[Doorkeeper], 8181))
    log.info("Server started")

    /* TEST IF DB EXISTS
    val name = "/user/test"
    system.actorSelection(name).resolveOne().onComplete {
      case Success(actor) => log.error(name + " exists")
      case Failure(ex) => log.error(name + " doesn't exist")
    }
    val name2 = "/user/main"
    system.actorSelection(name2).resolveOne().onComplete {
      case Success(actor) => log.error(name2 + " exists")
      case Failure(ex) => log.error(name2 + " doesn't exist")
    }
    */
  }

  /**
    * Load the list of usernames and password of the users who can access the server. The server will use these
    * informations to manage login queries.
    */
  private def loadUsers(): Unit = {
    users = new ConcurrentHashMap[String, String]()
    users.put("admin", "admin")
    users.put("user","user")
    log.info("Users loaded")
  }

  /**
    * Load user access permission data. Each user has specific access permissions for the databases present on the
    * server. Access to those database is filtered using these permissions.
    */
  private def loadUsersPermissions(): Unit = {
    permissions = new ConcurrentHashMap[String, ConcurrentHashMap[String, UserPermission]]
    val userP = new ConcurrentHashMap[String, UserPermission]
    userP.put("test", EnumPermission.Read)
    permissions.put("user",userP)
    log.info("Permissions loaded")
  }

  //* Loads databases */
  private def loadDatabases(s: ActorSystem): Unit = {
    storemanagers = new ConcurrentHashMap[String, ActorRef]()
    storemanagers.put("test", s.actorOf(Props[Storemanager], name="test"))

    val master = s.actorOf(Props[Storemanager], name="master")
    storemanagers.put("master", master)

    log.info("Databases loaded")
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