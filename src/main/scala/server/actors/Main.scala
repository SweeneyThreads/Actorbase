package server.actors

import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.Props
import server.Server
import server.enums.{EnumPermission, EnumReplyResult}
import server.enums.EnumPermission.UserPermission
import server.messages.internal.AskMapMessage
import server.messages.query.HelpMessages._
import server.messages.query.PermissionMessages.{NoPermissionMessage, ReadMessage, ReadWriteMessage}
import server.messages.query.{QueryMessage, ReplyMessage}
import server.messages.query.admin.ActorPropetiesMessages.ActorPropertiesMessage
import server.messages.query.admin.AdminMessage
import server.messages.query.admin.PermissionsManagementMessages.{AddPermissionMessage, ListPermissionMessage, PermissionsManagementMessage, RemovePermissionMessage}
import server.messages.query.admin.UsersManagementMessages.{AddUserMessage, ListUserMessage, RemoveUserMessage, UsersManagementMessage}
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.MapMessages.{MapMessage, SelectMapMessage}
import server.messages.query.user.RowMessages._
import server.messages.query.user.UserMessage
import server.utils.{Helper, ServerDependencyInjector, StandardServerInjector}

import scala.collection.JavaConversions._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** This actor executes client commands and checks permissions */
class Main(permissions: ConcurrentHashMap[String, UserPermission] = null, val server: ServerDependencyInjector = new StandardServerInjector {}) extends ReplyActor {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  var helper = new Helper

  var selectedDatabase = ""
  var selectedMap = ""

  def receive = {
    case m:QueryMessage => handleQueryMessage(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString(), "receive"))
  }

  private def handleQueryMessage(message: QueryMessage) = {
    message match {
      case m: UserMessage => handleUserMessage(m)
      case m: AdminMessage => handleAdminMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleQueryMessage"))
    }
  }

  private def handleUserMessage(message: UserMessage) = {
    message match {
      case m: HelpMessage => handleHelpMessage(m)
      case m: DatabaseMessage => handleDatabaseMessage(m)
      case m: MapMessage => handleMapMessage(m)
      case m: RowMessage => handleRowMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleUserMessage"))
    }
  }

  private def handleAdminMessage(message: AdminMessage) = {
    message match {
      case m:UsersManagementMessage => handleUserManagementMessage(m)
      case m:PermissionsManagementMessage => handlePermissionsManagementMessage(m)
      case m:ActorPropertiesMessage =>  handleActorPropertiesMessageMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleAdminMessage"))
    }
  }

  private def handleUserManagementMessage(message: UsersManagementMessage): Unit = {
    selectedDatabase = "master"
    selectedMap = "users"
    message match {
      case ListUserMessage() => handleRowMessage(new ListKeysMessage)
      case AddUserMessage(username: String, password:String) => handleRowMessage(new InsertRowMessage(username, password))
      case RemoveUserMessage(username: String) => handleRowMessage(new RemoveRowMessage(username))
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleUserManagementMessage"))
    }
  }

  private def handlePermissionsManagementMessage(message: PermissionsManagementMessage): Unit = {
    selectedDatabase = "master"
    selectedMap = "permissions"
    message match {
      case ListPermissionMessage(username:String) => //TODO
      case AddPermissionMessage(username: String, database:String, permissionType: UserPermission) => //TODO
      case RemovePermissionMessage(username: String, database:String) => //TODO
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handlePermissionsManagementMessage"))
    }
  }

  private def handleActorPropertiesMessageMessage(message: ActorPropertiesMessage): Unit = {
    message match {
      case _ => //TODO
    }
  }

  /** Manage help messages */
  private def handleHelpMessage(message: HelpMessage): Unit ={
    message match {
      case CompleteHelp() => reply(new ReplyMessage(EnumReplyResult.Done, message, new CompleteHelpReplyInfo(helper.completeHelp())))
      case SpecificHelp(command: String) => reply(new ReplyMessage(EnumReplyResult.Done, message, new SpecificHelpReplyInfo(helper.completeHelp())))
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleHelpMessage"))
    }
  }

  /** Manages database messages */
  private def handleDatabaseMessage(message: DatabaseMessage): Unit = {
    message match {
      case ListDatabaseMessage() => {
        val rep = new util.ArrayList[String]
        for (k: String <- server.getStoremanagers.keys())
          if (permissions == null || permissions.get(k) != null)
            rep.add(k)
        if (rep.isEmpty) reply("The server is empty")
        else reply(str.dropRight(1))
      }
      case SelectDatabaseMessage(name: String) => {
        if (!isValidStoremanager(name, message)) return
        selectedDatabase = name
        selectedMap = ""
        reply("Database " + name + " selected")
      }
      case CreateDatabaseMessage(name: String) => {
        if(!checkPermissions(message, name)) {
          reply(invalidOperationMessage)
          return
        }
        if(Server.storemanagers.containsKey(name)) {
          reply("A server with that name already exists")
          return
        }
        Server.storemanagers.put(name, context.actorOf(Props[Storemanager]))
        selectedDatabase = name
        logAndReply("Database " + name + " created" + "\nDatabase " + name + " selected")
      }
      case DeleteDatabaseMessage(name: String) => {
        if (!isValidStoremanager(name, message)) return
        server.getStoremanagers.remove(name)
        selectedDatabase = ""
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
      reply("Please select a map")
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
}