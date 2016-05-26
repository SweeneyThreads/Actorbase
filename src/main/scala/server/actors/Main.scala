package server.actors

import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.Props
import server.Server
import server.enums.{EnumPermission, EnumReplyResult}
import server.enums.EnumPermission.UserPermission
import server.messages.internal.AskMapMessage
import server.messages.query.HelpMessages._
import server.messages.query.PermissionMessages._
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

  /** Receive method */
  def receive = {
    case m:QueryMessage => handleQueryMessage(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString(), "receive"))
  }

  /** Handle query messages (User and Admin) */
  private def handleQueryMessage(message: QueryMessage) = {
    message match {
      case m: UserMessage => handleUserMessage(m)
      case m: AdminMessage => handleAdminMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleQueryMessage"))
    }
  }

  /** Handle user query messages (Help, Database, Map and Row) */
  private def handleUserMessage(message: UserMessage) = {
    message match {
      case m: HelpMessage => handleHelpMessage(m)
      case m: DatabaseMessage => handleDatabaseMessage(m)
      case m: MapMessage => handleMapMessage(m)
      case m: RowMessage => handleRowMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleUserMessage"))
    }
  }

  /** Handle admin query messages (UserManagement, PermissionManagement and Properties) */
  private def handleAdminMessage(message: AdminMessage) = {
    message match {
      case m:UsersManagementMessage => handleUserManagementMessage(m)
      case m:PermissionsManagementMessage => handlePermissionsManagementMessage(m)
      case m:ActorPropertiesMessage =>  handleActorPropertiesMessageMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleAdminMessage"))
    }
  }

  /** Handle user management messages (ListUser, AddUser and RemoveUser) */
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

  /** Handle permission management messages (ListPermission, AddPermission, RemovePermission) */
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

  /** Handle actors' properties messages */
  private def handleActorPropertiesMessageMessage(message: ActorPropertiesMessage): Unit = {
    message match {
      case _ => //TODO
    }
  }

  /** Handle help messages (complete or specific ones) */
  private def handleHelpMessage(message: HelpMessage): Unit ={
    message match {
      case CompleteHelp() => reply(ReplyMessage(EnumReplyResult.Done, message, CompleteHelpReplyInfo(helper.completeHelp())))
      case SpecificHelp(command: String) => reply(ReplyMessage(EnumReplyResult.Done, message, SpecificHelpReplyInfo(helper.completeHelp())))
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleHelpMessage"))
    }
  }

  /** Handle database messages (ListDatabase, SelectDatabase, CreateDatabase and DeleteDatabase) */
  private def handleDatabaseMessage(message: DatabaseMessage): Unit = {
    message match {
      // If the user types 'listdb'
      case ListDatabaseMessage() => {
        val dbs = List()
        // Foreach database
        for (k: String <- server.getStoremanagers.keys())
          // If the user is a super admin or has permissions on the current database, add the db name to the list
          if (permissions == null || permissions.get(k) != null) dbs.add(k)
        // If the database is empty it return a 'no dbs error'
        if (dbs.isEmpty) reply(ReplyMessage(EnumReplyResult.Error, message, NoDBInfo()))
        // Otherwise it return the list of dbs
        else reply(ReplyMessage(EnumReplyResult.Done, message, ListDBInfo(dbs)))
      }
      // If the user types 'selectdb <db_name>'
      case SelectDatabaseMessage(name: String) => {
        // If the selected database doesn't exists
        if(!server.getStoremanagers.containsKey(name)) reply(ReplyMessage(EnumReplyResult.Error, message, DBDoesNotExistInfo()))
        // If the user doesn't have at least read permissions on the selected database
        else if(checkPermissions(message, name)) reply(ReplyMessage(EnumReplyResult.Error, message, NoReadPermissionInfo()))
        // If the selected database exists and the user has at least read permissions on it
        else {
          // Select the database and reset the selected map
          selectedDatabase = name
          selectedMap = ""
          reply(ReplyMessage(EnumReplyResult.Done, message))
        }
      }
      // If the user types 'createdb <db_name>'
      case CreateDatabaseMessage(name: String) => {
        // If the selected database already exists
        if(server.getStoremanagers.containsKey(name)) reply(ReplyMessage(EnumReplyResult.Error, message, DBAlreadyExistInfo()))
        // If the selected database doesn't exist
        else {
          // Add the new database
          Server.storemanagers.put(name, context.actorOf(Props[Storemanager]))
          logAndReply(ReplyMessage(EnumReplyResult.Done, message))
        }
      }
      // If the user types 'deletedb <db_name>'
      case DeleteDatabaseMessage(name: String) => {
        // If the selected database doesn't exists
        if(!server.getStoremanagers.containsKey(name)) reply(ReplyMessage(EnumReplyResult.Error, message, DBDoesNotExistInfo()))
        // If the user doesn't have write permissions on the selected database
        else if(checkPermissions(message, name)) reply(ReplyMessage(EnumReplyResult.Error, message, NoWritePermissionInfo()))
        // If the selected database exists and the user has write permissions on it
        else {
          // Remove the database
          server.getStoremanagers.remove(name)
          // Deselect the database
          selectedDatabase = ""
          logAndReply(ReplyMessage(EnumReplyResult.Done, message))
        }
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