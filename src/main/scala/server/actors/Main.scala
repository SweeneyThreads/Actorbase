package server.actors

import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Deploy, Props}
import akka.remote.RemoteScope
import server.Server
import server.enums.{EnumPermission, EnumReplyResult}
import server.enums.EnumPermission.UserPermission
import server.messages.internal.AskMessages.AskMapMessage
import server.messages.query.HelpMessages._
import server.messages.query.PermissionMessages._
import server.messages.query.{QueryMessage, ReplyMessage}
import server.messages.query.admin.ActorPropetiesMessages.ActorPropertiesMessage
import server.messages.query.admin.AdminMessage
import server.messages.query.admin.PermissionsManagementMessages.{AddPermissionMessage, ListPermissionMessage, PermissionsManagementMessage, RemovePermissionMessage}
import server.messages.query.admin.UsersManagementMessages.{AddUserMessage, ListUserMessage, RemoveUserMessage, UsersManagementMessage}
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.MapMessages.{MapDoesNotExistInfo, MapMessage, NoMapSelectedInfo, SelectMapMessage}
import server.messages.query.user.RowMessages._
import server.messages.query.user.UserMessage
import server.utils.{Helper, ServerDependencyInjector, StandardServerInjector}

import scala.collection.JavaConversions._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/**
  * Actor that executes the messages from the client.
  * It processes database-level queries and admin-level queries by itself,
  * all other queries are sent to the right actor.
  *
  * @param permissions the user's permissions list.
  * @param server the server reference
  */
class Main(permissions: ConcurrentHashMap[String, UserPermission] = null, val server: ServerDependencyInjector = new StandardServerInjector {}) extends ReplyActor {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._
  // Values for futures
  // Instance of Helper class
  var helper = new Helper
  // Values for selected database and selected map
  var selectedDatabase = ""
  var selectedMap = ""

  /**
    * Processes all incoming messages.
    * It handles only QueryMessage messages
    */
  def receive = {
    // If it's a query message
    case m:QueryMessage => handleQueryMessage(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString(), "receive"))
  }

  /**
    * Processes QueryMessage messages.
    * Handles UserMessage messages and AdminMessage ones
    * calling the right method for each one.
    *
    * @param message The QueryMessage message to precess.
    */
  private def handleQueryMessage(message: QueryMessage) = {
    message match {
      // If it's a user type command
      case m: UserMessage => handleUserMessage(m)
      // If it's an admin type command
      case m: AdminMessage => handleAdminMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleQueryMessage"))
    }
  }

  /**
    * Processes UserMessage messages.
    * Handles HelpMessage, DatabaseMessage, MapMessage and RowMessage
    * messages calling the right method for each one.
    *
    * @param message The UserMessage message to precess.
    */
  private def handleUserMessage(message: UserMessage) = {
    message match {
      // If it's an help message
      case m: HelpMessage => handleHelpMessage(m)
      // If it's a database level message
      case m: DatabaseMessage => handleDatabaseMessage(m)
      // If it's a map level message
      case m: MapMessage => handleMapMessage(m)
      // If it's a row level message
      case m: RowMessage => handleRowMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleUserMessage"))
    }
  }

  /**
    * Processes AdminMessage messages.
    * Handles UsersManagementMessage, PermissionsManagementMessage and ActorPropertiesMessage
    * messages calling the right method for each one.
    *
    * @param message The AdminMessage message to precess.
    */
  private def handleAdminMessage(message: AdminMessage) = {
    message match {
      // If it's an user management command
      case m:UsersManagementMessage => handleUserManagementMessage(m)
      // If it's an permission management command
      case m:PermissionsManagementMessage => handlePermissionsManagementMessage(m)
      // If it's an actor property command
      case m:ActorPropertiesMessage =>  handleActorPropertiesMessageMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleAdminMessage"))
    }
  }

  /**
    * Processes UsersManagementMessage messages.
    * Handles ListUserMessage messages returning the list of users,
    * AddUserMessage messages adding an user and
    * RemoveUserMessage messages removing the user.
    *
    * @param message The UsersManagementMessage message to precess.
    */
  private def handleUserManagementMessage(message: UsersManagementMessage): Unit = {
    // Select the 'master' database and the 'user' map
    selectedDatabase = "master"
    selectedMap = "users"
    message match {
      // If the user types 'listuser'
      case ListUserMessage() => handleRowMessage(new ListKeysMessage)
      // If the user types 'adduser <username> <password>'
      case AddUserMessage(username: String, password:String) => handleRowMessage(new InsertRowMessage(username, password.getBytes("UTF-8")))
      // If the user types 'removeuser <username>'
      case RemoveUserMessage(username: String) => handleRowMessage(new RemoveRowMessage(username))
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleUserManagementMessage"))
    }
  }

  /**
    * Processes PermissionsManagementMessage messages.
    * Handles ListPermissionMessage messages returning the list of user's permissions,
    * AddPermissionMessage messages adding an user's permission and
    * RemovePermissionMessage messages removing the user's permission.
    *
    * @param message The PermissionsManagementMessage message to precess.
    */
  private def handlePermissionsManagementMessage(message: PermissionsManagementMessage): Unit = {
    // Select the 'master' database and the 'permissions' map
    selectedDatabase = "master"
    selectedMap = "permissions"
    message match {
      // If the user types 'listpermission <username>'
      case ListPermissionMessage(username:String) => //TODO
      // If the user types 'addpermission <username> <database> <permission_type>'
      case AddPermissionMessage(username: String, database:String, permissionType: UserPermission) => //TODO
      // If the user types 'removepermission <username> <database>'
      case RemovePermissionMessage(username: String, database:String) => //TODO
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handlePermissionsManagementMessage"))
    }
  }

  /**
    * Processes ActorPropertiesMessage messages.
    * //TODO
    *
    * @param message The ActorPropertiesMessage message to precess.
    */
  private def handleActorPropertiesMessageMessage(message: ActorPropertiesMessage): Unit = {
    message match {
      case _ => //TODO
    }
  }

  /**
    * Processes HelpMessage messages.
    * Handles CompleteHelp messages returning the list commands given by the Helper class and
    * SpecificHelp messages returning the description of single command given by the Helper class.
    *
    * @param message The HelpMessage message to precess.
    */
  private def handleHelpMessage(message: HelpMessage): Unit ={
    message match {
      // If the user types 'help' use the 'completeHelp' method from the Helper
      case CompleteHelp() => reply(ReplyMessage(EnumReplyResult.Done, message, CompleteHelpReplyInfo(helper.completeHelp())))
      // If the user types 'help <command>' use the 'specificHelp' method from the Helper
      case SpecificHelp(command: String) => reply(ReplyMessage(EnumReplyResult.Done, message, SpecificHelpReplyInfo(helper.specificHelp(command))))
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleHelpMessage"))
    }
  }

  /**
    * Processes DatabaseMessage messages.
    * Handles ListDatabaseMessage messages returning the list of databases
    * the user has, at least, read permissions on.
    * Handles SelectDatabaseMessage messages saving the selected database.
    * Handles CreateDatabaseMessage messages creating a new Storemanager which represent the new database.
    * Handles DeleteDatabaseMessage messages deleting the Storemanager which represent the database.
    *
    * @param message The DatabaseMessage message to precess.
    */
  private def handleDatabaseMessage(message: DatabaseMessage): Unit = {
    message match {
      // If the user types 'listdb'
      case ListDatabaseMessage() => {
        var dbs = List[String]()
        // Foreach database
        for (k: String <- server.getStoremanagers.keys())
          // If the user is a super admin or has permissions on the current database, add the db name to the list
          if (permissions == null || permissions.get(k) != null) dbs = dbs.::(k)
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
        else if(!checkPermissions(message, name)) reply(ReplyMessage(EnumReplyResult.Error, message, NoReadPermissionInfo()))
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
          Server.storemanagers.put(name, context.actorOf(Props[Storemanager].withDeploy(Deploy(scope=RemoteScope(nextAddress)))))
          logAndReply(ReplyMessage(EnumReplyResult.Done, message))
        }
      }
      // If the user types 'deletedb <db_name>'
      case DeleteDatabaseMessage(name: String) => {
        // If the selected database doesn't exists
        if(!server.getStoremanagers.containsKey(name)) reply(ReplyMessage(EnumReplyResult.Error, message, DBDoesNotExistInfo()))
        // If the user doesn't have write permissions on the selected database
        else if(!checkPermissions(message, name)) reply(ReplyMessage(EnumReplyResult.Error, message, NoWritePermissionInfo()))
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

  /**
    * Processes MapMessage messages if a database is selected.
    * Handles SelectMapMessage messages asking to the right Storemanager
    * if it contains the asked map, if so it saves the selected map.
    * All other MapMessage messages are sent to the right Storemanager.
    *
    * @param message The MapMessage message to precess.
    */
  private def handleMapMessage(message: MapMessage): Unit = {
    // If there isn't a selected database
    if(selectedDatabase == "") reply(ReplyMessage(EnumReplyResult.Error, message, NoDBSelectedInfo()))
    // If the selected database doesn't exists
    else if(!server.getStoremanagers.containsKey(selectedDatabase)) reply(ReplyMessage(EnumReplyResult.Error, message, DBDoesNotExistInfo()))
    // It gets the right storemanager
    val sm = server.getStoremanagers.get(selectedDatabase)
    message match {
      // If the user types 'selectmap <db_name>'
      case SelectMapMessage(name: String) => {
        // If the user doesn't have at least read permissions on the selected database
        if(!checkPermissions(message, selectedDatabase)) reply(ReplyMessage(EnumReplyResult.Error, message, NoReadPermissionInfo()))
        // Save the original sender
        val oldSender = sender
        // Ask the storemanager if there is a map with that name and save the reply on a future
        val future = sm ? new AskMapMessage(name)
        future.onComplete {
          case Success(result) => {
            // If the storemanager contains the map
            if (result.asInstanceOf[Boolean]) {
              // Select the map
              selectedMap = name
              reply(ReplyMessage(EnumReplyResult.Done, message), oldSender)
            }
            // The storemanager doesn't contain the map
            else reply(ReplyMessage(EnumReplyResult.Error, message, MapDoesNotExistInfo()), oldSender)
          }
          case Failure(t) => log.error("Error sending message: " + t.getMessage)
        }
      }
      // If it's another type of map level message
      case _ => {
        // Save the original sender
        val origSender = sender
        // Send the message to the storemanager and save the reply in a future
        val future = sm ? message
        future.onComplete {
          // Reply the usermanger with the reply from the storemanager
          case Success(result) => logAndReply(result.asInstanceOf[ReplyMessage], origSender)
          case Failure(t) => log.error("Error sending message: " + t.getMessage)
        }
      }
    }
  }

  /**
    * Processes RowMessage messages if a a database and a map are selected.
    * All RowMessage messages are sent to the right Storemanager.
    *
    * @param message The RowMessage message to precess.
    */
  private def handleRowMessage(message: RowMessage): Unit = {
    // If there isn't a selected database
    if(selectedDatabase == "") reply(ReplyMessage(EnumReplyResult.Error, message, NoDBSelectedInfo()))
      // If there isn't a selected map
    else if(selectedMap == "") reply(ReplyMessage(EnumReplyResult.Error, message, NoMapSelectedInfo()))
    // If the selected database doesn't exists
    else if(!server.getStoremanagers.containsKey(selectedDatabase)) reply(ReplyMessage(EnumReplyResult.Error, message, DBDoesNotExistInfo()))
    // It gets the right storemanager
    val sm = server.getStoremanagers.get(selectedDatabase)
    // Save the original sender
    val origSender = sender
    // Send a StorefinderRowMessage to the storemanager and save the reply in a future
    val future = sm ? StorefinderRowMessage(selectedMap, message)
    future.onComplete {
      // Reply the usermanager with the reply from the storemanager
      case Success(result) => reply(result.asInstanceOf[ReplyMessage], origSender)
      case Failure(t) => log.error("Error sending message: " + t.getMessage)
    }
  }

  /**
    * Checks if the logged user has the permission to execute the query.
    * If the user is an admin it has all the the permissions,
    * otherwise it checks if the user has some permissions on the selected database.
    * If permissions are found and are equal or greater than what the query needs,
    * it returns true otherwise false.
    *
    * @param message The message sent to the actor.
    * @param dbName The database selected by the client.
    * @return Return true if the user has the permission to execute the query.
    */
  private def checkPermissions(message: QueryMessage, dbName:String): Boolean = {
    //TODO admin permissions

    // If permissions are null the user is a super admin and has every permissions
    if(permissions == null)
      return true
    return message match {
      // If the message requires write permissions
      case n: ReadWriteMessage => {
        // Get the database permission
        val p = permissions.get(dbName)
        // Return if the user has a write permissions
        p != null && p == EnumPermission.ReadWrite
      }
      // If the message requires read permissions
      case n: ReadMessage => {
        // Get the database permission
        val p = permissions.get(dbName)
        // Return if the user has a read or write permissions
        p != null && (p == EnumPermission.Read || p == EnumPermission.ReadWrite)
      }
      // If the message doesn't require permissions
      case n: NoPermissionMessage => true
    }
  }
}