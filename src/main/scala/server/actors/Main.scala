package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import server.EnumPermission.Permission
import server.messages._
import server.messages.internal.AskMapMessage
import server.messages.query.DatabaseMessages._
import server.messages.query.MapMessages.{MapMessage, SelectMapMessage}
import server.messages.query.PermissionMessages.{NoPermissionMessage, ReadMessage, ReadWriteMessage}
import server.messages.query.RowMessages.{RowMessage, StorefinderRowMessage}
import server.messages.query._
import server.{EnumPermission, Server}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** This actor executes client commands and checks permissions */
class Main(permissions: ConcurrentHashMap[String, Permission] = null) extends Actor with akka.actor.ActorLogging {

  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  var selectedDatabase = ""
  var selectedMap = ""

  def receive = {
    case m: DatabaseMessage => manageDatabaseMessage(m)
    case m: MapMessage => manageNotDatabaseMessage(m)
    case m: RowMessage => manageNotDatabaseMessage(m)
  }

  /** Manages database messages */
  private def manageDatabaseMessage(message: DatabaseMessage): Unit = {
    message match {
      case ListDatabaseMessage() => {
        var str:String = ""
        for (k: String <- Server.storemanagers.keys()) {
          if (permissions == null || permissions.get(k) != null)
            str += k + " "
        }
        if(str == "") reply("No databases")
        else reply(str)
      }
      case SelectDatabaseMessage(name: String) => {
        if (Server.storemanagers.containsKey(name) && checkPermissions(message, name)) {
          selectedDatabase = name
          selectedMap = ""
          reply("Database " + name + " selected")
        }
        reply("Invalid operation")
      }
      case CreateDatabaseMessage(name: String) => {
        if (Server.storemanagers.containsKey(name) && checkPermissions(message, name)) {
          Server.storemanagers.put(name, context.actorOf(Props[Storemanager]))
          reply("Database " + name + " created")
          log.info("Database " + name + " created")
        }
        reply("Invalid operation")
      }
      case DeleteDatabaseMessage(name: String) => {
        if (Server.storemanagers.containsKey(name) && checkPermissions(message, name)) {
          Server.storemanagers.remove(name)
          reply("Database " + name + " deleted")
          log.info("Database " + name + " deleted")
        }
        reply("Invalid operation")
      }
    }
  }

  /** Manages map or row messages */
  private def manageNotDatabaseMessage(message: ActorbaseMessage): Unit = {
    if (selectedDatabase != "") {
      // If the database exists and the user has the permissions for the operation
      if (Server.storemanagers.containsKey(selectedDatabase) && checkPermissions(message, selectedDatabase)) {
        // Gets the right storemanager
        val storemanager = Server.storemanagers.get(selectedDatabase)
        message match {
          case m: MapMessage => manageMapMessage(m, storemanager)
          case m: RowMessage => manageRowMessage(m, storemanager)
        }
      }
      else reply("Invalid operation")
    }
    else reply("Please select a database")
  }

  /** Manages map messages */
  private def manageMapMessage(message: MapMessage, storemanager: ActorRef): Unit = {
    message match {
      // If it's a select command
      case SelectMapMessage(name: String) => {
        // Ask the storemanager if there's a map with that name
        val future = storemanager ? new AskMapMessage(name)
        // Save the original sender
        val oldSender = sender
        future.onComplete {
          case Success(result) => {
            // If the storemanager contains the map
            if (result.asInstanceOf[Boolean]) {
              selectedMap = name
              reply("Map " + name + " selected", oldSender)
            }
            else reply("Invalid map", oldSender)
          }
          case Failure(t) => log.error("Error sending message: " + t.getMessage)
        }
      }
      case _ => {
        val origSender = sender
        val future = storemanager ? message
        future.onComplete {
          case Success(result) => reply(result.toString, origSender)
          case Failure(t) => log.error("Error sending message: " + t.getMessage)
        }
      }
    }
  }

  /** Manages row messages */
  private def manageRowMessage(message: RowMessage, storemanager: ActorRef): Unit = {
    if (selectedMap != "") {
      val origSender = sender
      val future = storemanager ? StorefinderRowMessage(selectedMap, message)
      future.onComplete {
        case Success(result) => reply(result.toString, origSender)
        case Failure(t) => log.error("Error sending message: " + t.getMessage)
      }
    }
    else reply("Please select a map first")
  }

  /** Checks user permissions */
  private def checkPermissions(message: ActorbaseMessage, dbName:String): Boolean = {
    if(permissions == null)
      return true
    return message match {
      case n: ReadWriteMessage => {
        val p = permissions.get(dbName)
        p != null && p == EnumPermission.ReadWrite
      }
      case n: ReadMessage => {
        val p = permissions.get(dbName)
        p != null && (p == EnumPermission.Read || p == EnumPermission.ReadWrite)
      }
      case n: NoPermissionMessage => true
    }
  }

  private def reply(str:String, sender: ActorRef = sender): Unit = {
    Some(sender).map(_ ! str)
  }
}