package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import server.EnumPermission.Permission
import server.messages._
import server.messages.internal.AskMapMessage
import server.messages.query.HelpMessages.{CompleteHelp, HelpMessage, SpecificHelp}
import server.messages.query.PermissionMessages.{NoPermissionMessage, ReadMessage, ReadWriteMessage}
import server.messages.query.QueryMessage
import server.messages.query.admin.AdminMessage
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.MapMessages.{MapMessage, SelectMapMessage}
import server.messages.query.user.RowMessages.{RowMessage, StorefinderRowMessage}
import server.messages.query.user.UserMessage
import server.util.{StandardServerInjector, ServerDependencyInjector, Helper}
import server.{EnumPermission, Server}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** This actor executes client commands and checks permissions */
class Main(permissions: ConcurrentHashMap[String, Permission] = null, val server: ServerDependencyInjector = new StandardServerInjector {}) extends Actor with akka.actor.ActorLogging {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  var helper = new Helper

  var selectedDatabase = ""
  var selectedMap = ""

  val invalidOperationMessage = "Invalid operation"
  val unhandledMessage = "Unhandled message in main "

  def receive = {
    case m:QueryMessage => handleQueryMessage(m)
    case other => log.error(unhandledMessage + ", receive: " + other)
  }

  private def handleQueryMessage(message: QueryMessage) = {
    message match {
      case m: UserMessage => handleUserMessage(m)
      case m: AdminMessage => handleAdminMessage(m)
      case _ => log.error(unhandledMessage + ", handleQueryMessage: " + message)
    }
  }

  private def handleUserMessage(message: UserMessage) = {
    message match {
      case m: HelpMessage => handleHelpMessage(m)
      case m: DatabaseMessage => handleDatabaseMessage(m)
      case m: MapMessage => handleMapMessage(m)
      case m: RowMessage => handleRowMessage(m)
      case _ => log.error(unhandledMessage + ", handleUserMessage: " + message)
    }
  }

  private def handleAdminMessage(message: AdminMessage) = {
    //TODO admin commands
  }

  /** Manage help messages */
  private def handleHelpMessage(message: HelpMessage): Unit ={
    message match {
      case CompleteHelp() => reply(helper.CompleteHelp())
      case SpecificHelp(command: String) => reply(helper.SpecificHelp(command))
      case _ => log.error(unhandledMessage + ", handleHelpMessage: " + message)
    }
  }

  /** Manages database messages */
  private def handleDatabaseMessage(message: DatabaseMessage): Unit = {
    message match {
      case ListDatabaseMessage() => {
        var str: String = ""
        for (k: String <- server.getStoremanagers.keys())
          if (permissions == null || permissions.get(k) != null)
            str += k + " "
        if (str == "")
          reply("The server is empty")
        else reply(str)
      }
      case SelectDatabaseMessage(name: String) => {
        if (!isValidStoremanager(name, message)) return
        selectedDatabase = name
        selectedMap = ""
        reply("Database " + name + " selected")
      }
      case CreateDatabaseMessage(name: String) => {
        if (!isValidStoremanager(name, message)) return
        server.getStoremanagers.put(name, context.actorOf(Props[Storemanager]))
        logAndReply("Database " + name + " created")
      }
      case DeleteDatabaseMessage(name: String) => {
        if (!isValidStoremanager(name, message)) return
        server.getStoremanagers.remove(name)
        logAndReply("Database " + name + " deleted")
      }
    }
  }

  /** Manages map messages */
  private def handleMapMessage(message: MapMessage): Unit = {
    if(selectedDatabase == "") {
      reply("Please select a database")
      return
    }
    if(!isValidStoremanager(selectedDatabase, message)) {
      reply(invalidOperationMessage)
      return
    }
    val sm = server.getStoremanagers.get(selectedDatabase)

    message match {
      // If it's a select command
      case SelectMapMessage(name: String) => {
        // Ask the storemanager if there's a map with that name
        val future = sm ? new AskMapMessage(name)
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
        val future = sm ? message
        future.onComplete {
          case Success(result) => logAndReply(result.toString, origSender)
          case Failure(t) => log.error("Error sending message: " + t.getMessage)
        }
      }
    }
  }

  /** Manages row messages */
  private def handleRowMessage(message: RowMessage): Unit = {
    if (selectedDatabase == "") {
      reply("Please select a database")
      return
    }
    if (selectedMap == "") {
      reply("Please select a database")
      return
    }
    if (!isValidStoremanager(selectedDatabase, message)) {
      reply(invalidOperationMessage)
      return
    }
    val sm = server.getStoremanagers.get(selectedDatabase)

    val origSender = sender
    val future = sm ? StorefinderRowMessage(selectedMap, message)
    future.onComplete {
      case Success(result) => reply(result.toString, origSender)
      case Failure(t) => log.error("Error sending message: " + t.getMessage)
    }
  }

  private def isValidStoremanager(name:String, message:QueryMessage): Boolean = {
    val ris = server.getStoremanagers.containsKey(name) && checkPermissions(message, name)
    if(!ris) reply(invalidOperationMessage)
    return ris
  }

  /** Checks user permissions */
  private def checkPermissions(message: QueryMessage, dbName:String): Boolean = {
    //TODO admin permissions

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

  private def logAndReply(str:String, sender: ActorRef = sender): Unit = {
    log.info(str)
    reply(str, sender)
  }

  private def reply(str:String, sender: ActorRef = sender): Unit = Some(sender).map(_ ! str)
}