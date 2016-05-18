package server

import java.io.File
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config.ConfigFactory
import server.actors.{Doorkeeper, Storemanager}
import server.enums.EnumPermission
import server.enums.EnumPermission.UserPermission
import server.messages.query.user.MapMessages.CreateMapMessage
import server.utils.FileReader

/**
  * Created by matteobortolazzo on 02/05/2016.
  */



/** Startup class */
object Server extends App {
  var system:ActorSystem = null
  var log:LoggingAdapter = null
  var fileReader:FileReader = null

  var storemanagers: ConcurrentHashMap[String, ActorRef] = null
  var users: ConcurrentHashMap[String, String] = null
  var permissions: ConcurrentHashMap[String, ConcurrentHashMap[String, UserPermission]] = null

  override def main(args: Array[String]) {

    system = ActorSystem("System")
    log = Logging.getLogger(system, this)
    fileReader = new FileReader(log)

    loadUsers()
    loadUsersPermissions()
    loadDatabases(system)
    system.actorOf(Props(classOf[Doorkeeper], 8181))
    log.info("Server started")
  }

  //* Loads system users */
  private def loadUsers(): Unit = {
    users = fileReader.readUsers("accounts.json")
    users.put("admin", "admin")
    log.info("Users loaded")
  }

  //* Loads users permissions */
  private def loadUsersPermissions(): Unit = {
    permissions = fileReader.readPermissions("permissions.json")
    log.info("Permissions loaded")
  }

  //* Loads databases */
  private def loadDatabases(s: ActorSystem): Unit = {
    storemanagers = new ConcurrentHashMap[String, ActorRef]()
    storemanagers.put("test", s.actorOf(Props[Storemanager], name="test"))

    val master = s.actorOf(Props[Storemanager], name="master")
    storemanagers.put("master", master)
    master ! new CreateMapMessage("users")
    master ! new CreateMapMessage("permissions")

    log.info("Database loaded")
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