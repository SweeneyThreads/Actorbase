package server

import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import org.json._
import server.EnumPermission.Permission
import server.actors.{Doorkeeper, Storemanager}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** Permission types */
object EnumPermission {
  val permissionsType = Seq(Read, ReadWrite)

  sealed trait Permission

  case object Read extends Permission

  case object ReadWrite extends Permission

}

/** Startup class */
object Server extends App {

  var storemanagers: ConcurrentHashMap[String, ActorRef] = null
  var users: ConcurrentHashMap[String, String] = null
  var permissions: ConcurrentHashMap[String, ConcurrentHashMap[String, Permission]] = null

  override def main(args: Array[String]) {
    val system = ActorSystem("System")
    loadUsers()
    loadUsersPermissions()
    loadDatabases(system)
    system.actorOf(Props(classOf[Doorkeeper], 8181))
    println("Server started")
  }

  //* Loads system users */
  private def loadUsers(): Unit = {
    users = new ConcurrentHashMap[String, String]()

    try {
      //* Open the file that should be on the same level as SRC folder */
      val source = scala.io.Source.fromFile("accounts.json")
      //* Loads the list of user from the file and close the file */
      val list = try source.getLines().mkString finally source.close()
      val jsonObject = new JSONObject(list)
      val accounts = jsonObject.getJSONArray("accounts")
      for (i <- 0 until accounts.length()) {
        val singleAccount = accounts.getJSONObject(i)
        val id = singleAccount.getString("id")
        val pw = singleAccount.getString("pw")
        users.put(id, pw)
      }
    } catch {
      case e: Exception => {
        addDefaultUser()
        e.printStackTrace()
      }
    }
  }

  //* Loads users permissions */
  private def loadUsersPermissions(): Unit = {
    permissions = new ConcurrentHashMap[String, ConcurrentHashMap[String, Permission]]()

    try {
      //* Open the file that should be on the same level as SRC folder */
      val source = scala.io.Source.fromFile("permissions.json")
      //* Loads the list of user from the file and close the file */
      val list = try source.getLines().mkString finally source.close()

      val jsonObject = new JSONObject(list)
      val permissionsList = jsonObject.getJSONArray("permissions")
      for (i <- 0 until permissionsList.length()) {
        val singleUserEntry = permissionsList.getJSONObject(i)
        val accountID = singleUserEntry.getString("id")
        val permissionList = singleUserEntry.getJSONArray("list")
        val permissionsMap = new ConcurrentHashMap[String, Permission]()
        for (j <- 0 until permissionList.length()) {
          val singleDbPerm = permissionList.getJSONObject(j)
          val DBName = singleDbPerm.getString("name")
          val permOnDB = singleDbPerm.getInt("perm")
          if (permOnDB == 0)
            permissionsMap.put(DBName, EnumPermission.Read)
          else
            permissionsMap.put(DBName, EnumPermission.ReadWrite)
          permissions.put(accountID, permissionsMap)
        }
      }
    } catch {
      case e: Exception => {
        addDefaultPermissions()
        e.printStackTrace()
      }
    }
  }

  //* Loads databases */
  private def loadDatabases(s: ActorSystem): Unit = {
    storemanagers = new ConcurrentHashMap[String, ActorRef]()
    storemanagers.put("test", s.actorOf(Props[Storemanager]))
  }

  private def addDefaultUser(): Unit = {
    users.put("admin", "admin")
  }

  private def addDefaultPermissions(): Unit = {
    val perm = new ConcurrentHashMap[String, Permission]()
    perm.put("test", EnumPermission.ReadWrite)
    permissions.put("admin", perm)
  }
}

/* RICETTA PER LA FELICITA'

* Fai partire il server (Debug).
* Fai partire il client (Run).
* Visualizza entrambe le finestre contemporaneamente.
* Scrivi 'connect localhost:8181 admin admin'
* Divertiti con i comandi che Borto ha implementato per te!
*
* listdb, selectdb nome, createdb nome, deletedb nome,
* list, select nome, create nome, delete nome,
* insert 'key' value, update 'key' value, remove 'key', find 'key'.
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