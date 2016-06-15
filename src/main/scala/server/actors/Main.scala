
/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 SWEeneyThreads
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 *
 * @author SWEeneyThreads
 * @version 0.0.1
 * @since 0.0.1
 */

package server.actors

import java.util

import akka.actor.{Deploy, Props}
import akka.pattern.ask
import akka.remote.RemoteScope
import server.StaticSettings
import server.enums.EnumActorsProperties.ActorProperties
import server.enums.EnumPermission.UserPermission
import server.enums.{EnumActorsProperties, EnumPermission, EnumReplyResult}
import server.messages.internal.AskMessages.AskMapMessage
import server.messages.query.PermissionMessages._
import server.messages.query.admin.AdminMessage
import server.messages.query.admin.PermissionsManagementMessages._
import server.messages.query.admin.SettingsMessages._
import server.messages.query.admin.UsersManagementMessages._
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.HelpMessages._
import server.messages.query.user.MapMessages._
import server.messages.query.user.RowMessages._
import server.messages.query.user.UserMessage
import server.messages.query.{QueryMessage, ReplyErrorInfo, ReplyMessage, ServiceErrorInfo}
import server.utils.{ConfigurationManager, Helper, Serializer}

import scala.collection.JavaConversions._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Actor that executes the messages from the client.
  * It processes database-level queries and admin-level queries by itself,
  * all other queries are sent to the right actor.
  * It's the only actor that interacts with the Usermanger actor, every reply it's sent to it.
  *
  * @constructor Create a new Main actor instance from a ConcurrentHashMap and a ServerDependencyInjector.
  * @see UserPermission
  * @see Server
  */
class Main(perms: util.HashMap[String, UserPermission] = null) extends ReplyActor {

  // Instance of Helper class
  val helper = new Helper

  // Values for selected database and selected map
  var selectedDatabase = ""
  var selectedMap = ""

  /**
    * Processes all incoming messages.
    * It handles only QueryMessage messages.
    *
    * @see QueryMessage
    * @see #handleQueryMessage(QueryMessage)
    */
  def receive = {
    // If it's a query message
    case m: QueryMessage => handleQueryMessage(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString(), "receive"))
  }

  /**
    * Processes QueryMessage messages.
    * Handles UserMessage messages and AdminMessage ones
    * calling the right method for each one.
    *
    * @param message The QueryMessage message to process.
    * @see QueryMessage
    * @see UserMessage
    * @see AdminMessage
    * @see #handleUserMessage(UserMessage)
    * @see #handleAdminMessage(AdminMessage)
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
    * @param message The UserMessage message to process.
    * @see UserMessage
    * @see HelpMessage
    * @see DatabaseMessage
    * @see MapMessage
    * @see RowMessage
    * @see #handleHelpMessage(HelpMessage)
    * @see #handleDatabaseMessage(DatabaseMessage)
    * @see #handleMapMessage(MapMessage)
    * @see #handleRowMessage(RowMessage)
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
    * Processes HelpMessage messages.
    * Handles CompleteHelpMessage messages returning the list commands given by the Helper class and
    * SpecificHelpMessage messages returning the description of single command given by the Helper class.
    *
    * @param message The HelpMessage message to process.
    * @see CompleteHelpMessage
    * @see SpecificHelpMessage
    * @see ReplyMessage
    */
  private def handleHelpMessage(message: HelpMessage): Unit = {
    message match {
      // If the user types 'help' use the 'completeHelp' method from the Helper
      case CompleteHelpMessage() => reply(ReplyMessage(EnumReplyResult.Done, message, CompleteHelpReplyInfo(helper.completeHelp())))
      // If the user types 'help <command>' use the 'specificHelp' method from the Helper
      case SpecificHelpMessage(command: String) => reply(ReplyMessage(EnumReplyResult.Done, message, SpecificHelpReplyInfo(helper.specificHelp(command))))
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleHelpMessage"))
    }
  }

  /**
    * Processes DatabaseMessage messages.
    * Handles ListDatabaseMessage messages returning the list of databases
    * the user has, at least, read permissions on.
    * Handles SelectDatabaseMessage messages saving the selected database.
    * Handles CreateDatabaseMessage messages creating a new Storemanager actor which represents the new database.
    * Handles DeleteDatabaseMessage messages deleting Storemanager actors that represent the database.
    *
    * @param message The DatabaseMessage message to process.
    * @see DatabaseMessage
    * @see ListDatabaseMessage
    * @see SelectDatabaseMessage
    * @see CreateDatabaseMessage
    * @see DeleteDatabaseMessage
    * @see Storemanager
    * @see ReplyMessage
    */
  private def handleDatabaseMessage(message: DatabaseMessage): Unit = {
    message match {
      // If the user types 'listdb'
      case ListDatabaseMessage() => {

          var dbs = List[String]()
          // Foreach database
          for (k: String <- StaticSettings.mapManagerRefs.keys())
          // If the user is a super admin or has permissions on the current database, add the db name to the list
            if (perms == null || perms.get(k) != null) dbs = dbs.::(k)
          // If the database is empty it return a 'no dbs error'
          if (dbs.isEmpty) reply(ReplyMessage(EnumReplyResult.Error, message, NoDBInfo()))
          // Otherwise it return the list of dbs
          else reply(ReplyMessage(EnumReplyResult.Done, message, ListDBInfo(dbs.sorted)))

      }
      // If the user types 'selectdb <db_name>'
      case SelectDatabaseMessage(name: String) => {

          // If the selected database doesn't exists
          if (!StaticSettings.mapManagerRefs.containsKey(name)) reply(ReplyMessage(EnumReplyResult.Error, message, DBDoesNotExistInfo()))
          // If the user doesn't have at least read permissions on the selected database
          else if (!checkPermissions(message, name)) reply(ReplyMessage(EnumReplyResult.Error, message, NoReadPermissionInfo()))
          // If the selected database exists and the user has at least read permissions on it
          else {
            // Select the database and reset the selected map
            selectedDatabase = name
            selectedMap = ""
            reply(ReplyMessage(EnumReplyResult.Done, message))
          }

      }
      // If the user types 'createdb <db_name>'
      case CreateDatabaseMessage(dbName: String) => {

          // If the selected database already exists
          if (StaticSettings.mapManagerRefs.containsKey(dbName)) reply(ReplyMessage(EnumReplyResult.Error, message, DBAlreadyExistInfo()))
          // If the selected database doesn't exist
          else {
            // Add the new database
            selectedDatabase = dbName
            context.system.actorOf(Props(new MapManager()).withDeploy(Deploy(scope = RemoteScope(nextAddress))), name = dbName)
            logAndReply(ReplyMessage(EnumReplyResult.Done, message))
          }

      }
      // If the user types 'deletedb <db_name>'
      case DeleteDatabaseMessage(name: String) => {

          // If the selected database doesn't exists
          if (!StaticSettings.mapManagerRefs.containsKey(name)) reply(ReplyMessage(EnumReplyResult.Error, message, DBDoesNotExistInfo()))
          // If the user doesn't have write permissions on the selected database
          else if (!checkPermissions(message, name)) reply(ReplyMessage(EnumReplyResult.Error, message, NoWritePermissionInfo()))
          // If the selected database exists and the user has write permissions on it
          else {
            // Kills the actor and removes the reference
            val ref = StaticSettings.mapManagerRefs.get(name)
            context.stop(ref)
            StaticSettings.mapManagerRefs.remove(name)
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
    * @param message The MapMessage message to process.
    * @see MapMessage
    * @see SelectMapMessage
    * @see Storemanager
    */
  private def handleMapMessage(message: MapMessage): Unit = {
    // If there isn't a selected database
    if (selectedDatabase == "") reply(ReplyMessage(EnumReplyResult.Error, message, NoDBSelectedInfo()))
    // If the selected database doesn't exists
    else if (!StaticSettings.mapManagerRefs.containsKey(selectedDatabase)) reply(ReplyMessage(EnumReplyResult.Error, message, DBDoesNotExistInfo()))
    // It gets the right storemanager
    val sm = StaticSettings.mapManagerRefs.get(selectedDatabase)
    message match {
      // If the user types 'selectmap <db_name>'
      case SelectMapMessage(name: String) => {
        // If the user doesn't have at least read permissions on the selected database
        if (!checkPermissions(message, selectedDatabase)) reply(ReplyMessage(EnumReplyResult.Error, message, NoReadPermissionInfo()))
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
        if (!checkPermissions(message, selectedDatabase)) {
          if(message.isInstanceOf[ReadWriteMessage])
            reply(ReplyMessage(EnumReplyResult.Error, message, NoWritePermissionInfo()))
          else
            reply(ReplyMessage(EnumReplyResult.Error, message, NoReadPermissionInfo()))
        }
        else {
          // Save the original sender
          val origSender = sender
          // Send the message to the storemanager and save the reply in a future
          val future = sm ? message
          future.onComplete {
            // Reply the usermanger with the reply from the storemanager
            case Success(result) => {
              result.asInstanceOf[ReplyMessage].result match {
                case EnumReplyResult.Done => {
                  // Get which was the request ( CreateMapMessage or DeleteMapMessage )
                  val whoAsked = result.asInstanceOf[ReplyMessage].question
                  // If it was a CreateMapMessage, we mark that map as the selected map
                  if (whoAsked.isInstanceOf[CreateMapMessage])
                    selectedMap = whoAsked.asInstanceOf[CreateMapMessage].name
                  else if (whoAsked.isInstanceOf[DeleteMapMessage])
                  // Otherwise, if it was a DeleteMapMessage, we unmark that map as the selected one
                    selectedMap = ""
                }
                case EnumReplyResult.Error => {
                  // Do nothing at the moment, if this match case does not exist the Main will fail when receiving an
                  // EnumReplyResult.Error as response
                }
              }
              logAndReply(result.asInstanceOf[ReplyMessage], origSender)
            }
            case Failure(t) => log.error("Error sending message: " + t.getMessage)
          }
        }
      }
    }
  }

  /**
    * Processes RowMessage messages if a a database and a map are selected.
    * All RowMessage messages are sent to the right Storemanager.
    *
    * @param message The RowMessage message to process.
    * @see RowMessage
    * @see Storemanager
    */
  private def handleRowMessage(message: RowMessage): Unit = {
    // If there isn't a selected database
    if (selectedDatabase == "") reply(ReplyMessage(EnumReplyResult.Error, message, NoDBSelectedInfo()))
    // If there isn't a selected map
    else if (selectedMap == "") reply(ReplyMessage(EnumReplyResult.Error, message, NoMapSelectedInfo()))
    // If the selected database doesn't exists
    if (!checkPermissions(message, selectedDatabase)) {
      if(message.isInstanceOf[ReadWriteMessage])
        reply(ReplyMessage(EnumReplyResult.Error, message, NoWritePermissionInfo()))
      else
        reply(ReplyMessage(EnumReplyResult.Error, message, NoReadPermissionInfo()))
    }
    else {
      if (!StaticSettings.mapManagerRefs.containsKey(selectedDatabase))
        reply(ReplyMessage(EnumReplyResult.Error, message, DBDoesNotExistInfo()))
      // It gets the right storemanager
      val sm = StaticSettings.mapManagerRefs.get(selectedDatabase)
      // Save the original sender
      val origSender = sender
      // Send a StorefinderRowMessage to the storemanager and save the reply in a future
      val future = sm ? StorefinderRowMessage(selectedMap, message)
      future.onComplete {
        // Reply the usermanager with the reply from the storemanager
        case Success(result) => reply(result.asInstanceOf[ReplyMessage], origSender)
        case Failure(t) => {
          log.error("Error sending message: " + t.getMessage);
          reply(new ReplyMessage(EnumReplyResult.Error, message,
            new ServiceErrorInfo("Error sending message: " + t.getMessage)), origSender)
        }
      }
    }
  }

  /**
    * Processes AdminMessage messages.
    * Handles UsersManagementMessage, PermissionsManagementMessage and SettingMessage
    * messages calling the right method for each one.
    *
    * @param message The AdminMessage message to process.
    * @see AdminMessage
    * @see UsersManagementMessage
    * @see PermissionsManagementMessage
    * @see SettingMessage
    * @see #handleUserManagementMessage(UsersManagementMessage)
    * @see #handlePermissionsManagementMessage(PermissionsManagementMessage)
    * @see #handleSettingMessage(SettingsMessage)
    */
  private def handleAdminMessage(message: AdminMessage) = {
    message match {
      // If it's an user management command
      case m: UsersManagementMessage => handleUserManagementMessage(m)
      // If it's an permission management command
      case m: PermissionsManagementMessage => handlePermissionsManagementMessage(m)
      // If it's a setting command
      case m: SettingMessage => handleSettingMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleAdminMessage"))
    }
  }

  /**
    * Processes UsersManagementMessage messages, it query the 'users' map in the 'master' database.
    * Handles ListUserMessage messages returning the list of users in the map,
    * AddUserMessage messages adding an user in the map and
    * RemoveUserMessage messages removing from the map the user.
    *
    * @param message The UsersManagementMessage message to process.
    * @see UsersManagementMessage
    * @see ListUserMessage
    * @see AddUserMessage
    * @see RemoveUserMessage
    */
  private def handleUserManagementMessage(message: UsersManagementMessage): Unit = {
    // Select the 'master' database and the 'user' map
    selectedDatabase = "master"
    selectedMap = "users"
    message match {
      // If the user types 'listuser'
      case ListUserMessage() => handleListUserMessage(message.asInstanceOf[ListUserMessage])
      // If the user types 'adduser <username> <password>'
      case AddUserMessage(username: String, password: String) =>
        handleAddUserMessage(message.asInstanceOf[AddUserMessage], username, password)
      // If the user types 'removeuser <username>'
      case RemoveUserMessage(username: String) => handleRemoveUserMessage(message.asInstanceOf[RemoveUserMessage], username)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleUserManagementMessage"))
    }
  }

  /**
    * This method handle a 'listuser' command of admin. This command lets the admin have a list of all the user
    * in the 'users' map in the 'master' database. It through the list of users, printing the username of each one.
    * The command takes no parameters, as it just print the whole list of users.
    *
    * @param message The ListUserMessage to process.
    */
  private def handleListUserMessage(message: ListUserMessage): Unit = {
    val sm = StaticSettings.mapManagerRefs.get(selectedDatabase)
    val origSender = sender
    val future = sm ? StorefinderRowMessage(selectedMap, new ListKeysMessage())
    future.onComplete {
      // Reply the usermanager with the reply from the storemanager
      case Success(result) => {
        val resultMessage = result.asInstanceOf[ReplyMessage]
        selectedDatabase = ""
        selectedMap = ""
        resultMessage.result match {
          case EnumReplyResult.Done => {
            val userList: List[String] = resultMessage.info.asInstanceOf[ListKeyInfo].keys
            reply(ReplyMessage(EnumReplyResult.Done, message, new ListUserInfo(userList)), origSender)
          }
          case EnumReplyResult.Error => {
            if (resultMessage.info.isInstanceOf[NoKeysInfo])
              reply(ReplyMessage(EnumReplyResult.Error, message, new NoUserInfo()), origSender)
            else
              reply(ReplyMessage(EnumReplyResult.Error, message, new ReplyErrorInfo()), origSender)
          }
        }
      }
      case Failure(t) => {
        log.error("Error sending message: " + t.getMessage);
        reply(new ReplyMessage(EnumReplyResult.Error, message,
          new ServiceErrorInfo("Error sending message: " + t.getMessage)), origSender)
      }
    }
  }

  /**
    * This method add a user to the 'users' map in the 'master' database. It takes 3 parameters: an AddUserMessage
    * an username and a password.
    * Username & password are store in the map mentioned before. This method also add a List on 'permissions' map,
    * always in the 'master' database. The added List refer to the new user, and it's empty, so the user has no permissions
    * on any database.
    *
    * @param message The AddUser message to process.
    * @param username The name of new user to be add
    * @param password The password of new user to be add
    * @see RowMessage
    * @see Storemanager
    */
  private def handleAddUserMessage(message: AddUserMessage, username: String, password: String): Unit = {
    val sm = StaticSettings.mapManagerRefs.get(selectedDatabase)
    // Save the original sender
    val origSender = sender
    // Send a StorefinderRowMessage to the storemanager and save the reply in a future
    val future = sm ? StorefinderRowMessage(selectedMap, new InsertRowMessage(username, password.getBytes("UTF-8")))
    future.onComplete {
      // Reply the usermanager with the reply from the storemanager
      case Success(result) => {
        val resultMessage = result.asInstanceOf[ReplyMessage]
        resultMessage.result match {
          case EnumReplyResult.Done => {
            val singleUserPermissions: util.HashMap[String, EnumPermission.UserPermission] =
              new util.HashMap[String, EnumPermission.UserPermission]()
            val serializer: Serializer = new Serializer()
            val mapSerialized = serializer.serialize(singleUserPermissions)
            selectedMap = "permissions"
            handleRowMessage(new InsertRowMessage(username, mapSerialized))
            reply(ReplyMessage(EnumReplyResult.Done, message, new AddUserInfo()), origSender)
          }
          case EnumReplyResult.Error => {
            selectedMap = ""
            selectedDatabase = ""
            reply(ReplyMessage(EnumReplyResult.Error, message, new KeyAlreadyExistInfo()), origSender)
          }
        }
      }
      case Failure(t) => {
        log.error("Error sending message: " + t.getMessage);
        reply(new ReplyMessage(EnumReplyResult.Error, message,
          new ServiceErrorInfo("Error sending message: " + t.getMessage)), origSender)
      }
    }
  }

  /**
    * This method remove a user form the 'users' map in the 'master' database. It takes two parameters: the message to handle
    * and the username of the user to be removed.
    *
    * @param message The RemoveUser message to process.
    * @param username The name of user to be remove
    * @see RowMessage
    * @see Storemanager
    */
  private def handleRemoveUserMessage(message: RemoveUserMessage, username: String): Unit = {
    // It gets the right storemanager
    val sm = StaticSettings.mapManagerRefs.get(selectedDatabase)
    // Save the original sender
    val origSender = sender
    // Send a StorefinderRowMessage to the storemanager and save the reply in a future
    val future = sm ? StorefinderRowMessage(selectedMap, new RemoveRowMessage(username))
    future.onComplete {
      // Reply the usermanager with the reply from the storemanager
      case Success(result) => {
        val resultMessage = result.asInstanceOf[ReplyMessage]
        resultMessage.result match {
          case EnumReplyResult.Done => {
            selectedMap = "permissions"
            handleRowMessage(new RemoveRowMessage(username))
            selectedMap = ""
            selectedDatabase = ""
            reply(ReplyMessage(EnumReplyResult.Done, message, new RemoveUserInfo()), origSender)
          }
          case EnumReplyResult.Error => {
            selectedMap = ""
            selectedDatabase = ""
            reply(ReplyMessage(EnumReplyResult.Error, message, new KeyDoesNotExistInfo()), origSender)
          }
        }
      }
      case Failure(t) => {
        log.error("Error sending message: " + t.getMessage);
        reply(new ReplyMessage(EnumReplyResult.Error, message,
          new ServiceErrorInfo("Error sending message: " + t.getMessage)), origSender)
      }
    }

  }

  /**
    * Processes PermissionsManagementMessage messages, it query the 'permissions' map in the 'master' database.
    * Handles ListPermissionMessage messages returning the list of user's permissions in the map,
    * AddPermissionMessage messages adding an user's permission in the map and
    * RemovePermissionMessage messages removing the user's permission from the map.
    *
    * @param message The PermissionsManagementMessage message to process.
    * @see PermissionsManagementMessage
    * @see ListPermissionMessage
    * @see AddPermissionMessage
    * @see RemovePermissionMessage
    */
  private def handlePermissionsManagementMessage(message: PermissionsManagementMessage): Unit = {
    // Select the 'master' database and the 'permissions' map
    selectedDatabase = "master"
    selectedMap = "permissions"
    message match {
      // If the user types 'listpermission <username>'
      case ListPermissionMessage(username: String) => handlePermissionsListMessage(message.asInstanceOf[ListPermissionMessage])
      // If the user types 'addpermission <username> <database> <permission_type>'
      case AddPermissionMessage(username: String, database: String, permissionType: UserPermission) =>
        handleAddPermissionMessage(message.asInstanceOf[AddPermissionMessage])
      // If the user types 'removepermission <username> <database>'
      case RemovePermissionMessage(username: String, database: String) =>
        handleRemovePermissionsMessage(message.asInstanceOf[RemovePermissionMessage])
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handlePermissionsManagementMessage"))
    }
  }

  /**
    * This handle a ListPermissionMessage, it query the 'permission' map in the 'master database.
    * The message it handles has a parameter, which is the name of the user of which we want to show the list
    * of permissions.
    * This method, send a ReplyMessage which will print a list of databases with the permission of the user on each one.
    *
    * @param message the ListPermissionMessage to process.
    */
  private def handlePermissionsListMessage(message: ListPermissionMessage): Unit = {
    val sm = StaticSettings.mapManagerRefs.get(selectedDatabase)
    val origSender = sender
    val future = sm ? StorefinderRowMessage("permissions", new FindRowMessage(message.username))
    future.onComplete {
      case Success(result) => {
        val resultMessage = result.asInstanceOf[ReplyMessage]
        resultMessage.result match {
          case EnumReplyResult.Done => {
            val serializer: Serializer = new Serializer()
            val array = resultMessage.info.asInstanceOf[FindInfo].value
            val singleUserPermissions: util.HashMap[String, EnumPermission.UserPermission] =
              serializer.deserialize(array).asInstanceOf[util.HashMap[String, UserPermission]]
            selectedDatabase = ""
            selectedMap = ""
            reply(new ReplyMessage(EnumReplyResult.Done, message,
              new ListPermissionsInfo(singleUserPermissions)), origSender)
          }
          case EnumReplyResult.Error => {
            resultMessage.info match {
              case KeyDoesNotExistInfo() => {
                reply(new ReplyMessage(EnumReplyResult.Error, message, new NoKeysInfo()), origSender )
              }
            }
          }
        }
      }
      case Failure(t) => {
        log.error("Error sending message: " + t.getMessage);
        reply(new ReplyMessage(EnumReplyResult.Error, message,
          new ServiceErrorInfo("Error sending message: " + t.getMessage)), origSender)
      }
    }
  }

  /**
    * This query the 'permissions' map in 'master' database and add a permission related to a database and to a user.
    * The user gains the permission on that database. For example, if a user has no permission on a database, adding a Read
    * permission let the user query that database, getting info.
    *
    * @param message The AddPermissionMessage to process.
    */
  private def handleAddPermissionMessage(message: AddPermissionMessage): Unit = {
    val sm = StaticSettings.mapManagerRefs.get(selectedDatabase)
    val origSender = sender
    val future = sm ? StorefinderRowMessage(selectedMap, new FindRowMessage(message.username))
    future.onComplete {
      case Success(result) => {
        result.asInstanceOf[ReplyMessage].result match {
          case EnumReplyResult.Done => {
            val serializer: Serializer = new Serializer()
            val array = result.asInstanceOf[ReplyMessage].info.asInstanceOf[FindInfo].value
            val singleUserPermissions: util.HashMap[String, EnumPermission.UserPermission] =
              serializer.deserialize(array).asInstanceOf[util.HashMap[String, UserPermission]]
            singleUserPermissions.put(message.database, message.permissionType)
            val permissionsSerialized: Array[Byte] = serializer.serialize(singleUserPermissions)
            val replyMes = sm ? StorefinderRowMessage(selectedMap,
              new UpdateRowMessage(message.username, permissionsSerialized))
            replyMes.onComplete {
              case Success(result) => {
                result.asInstanceOf[ReplyMessage].result match {
                  case EnumReplyResult.Done => {
                    selectedDatabase = ""
                    selectedMap = ""
                    reply(ReplyMessage(EnumReplyResult.Done, message), origSender)
                  }
                  case EnumReplyResult.Error => {
                    selectedDatabase = ""
                    selectedMap = ""
                    reply(ReplyMessage(EnumReplyResult.Error, message, new KeyDoesNotExistInfo())) //should never get there
                  }
                }
              }
            }
          }
          case EnumReplyResult.Error => {
            if (result.asInstanceOf[ReplyMessage].info.isInstanceOf[KeyDoesNotExistInfo])
              reply(ReplyMessage(EnumReplyResult.Error, message, new KeyDoesNotExistInfo()), origSender)
          }
        }
      }
      case Failure(t) => {
        log.error("Error sending message: " + t.getMessage);
        reply(new ReplyMessage(EnumReplyResult.Error, message,
          new ServiceErrorInfo("Error sending message: " + t.getMessage)), origSender)
      }
    }
  }

  /**
    * This method handle a RemovePermissionMessage, which is going to remove permissions of a user on a given database.
    * Removing a user permission on a database means the user can no longer query or see the database.
    *
    * @param message The RemovePermissionMessage to process.
    */
  private def handleRemovePermissionsMessage(message: RemovePermissionMessage): Unit = {
    val sm = StaticSettings.mapManagerRefs.get(selectedDatabase)
    val origSender = sender
    val future = sm ? StorefinderRowMessage(selectedMap, new FindRowMessage(message.username))
    future.onComplete {
      case Success(result) => {
        result.asInstanceOf[ReplyMessage].result match {
          case EnumReplyResult.Done => {
            val serializer: Serializer = new Serializer()
            val array = result.asInstanceOf[ReplyMessage].info.asInstanceOf[FindInfo].value
            val singleUserPermissions: util.HashMap[String, EnumPermission.UserPermission] =
              serializer.deserialize(array).asInstanceOf[util.HashMap[String, UserPermission]]
            if (singleUserPermissions.remove(message.database) == null)
              reply(ReplyMessage(EnumReplyResult.Error, message, new KeyDoesNotExistInfo()), origSender)
            val permissionsSerialized: Array[Byte] = serializer.serialize(singleUserPermissions)
            val replyMes = sm ? StorefinderRowMessage(selectedMap,
              new UpdateRowMessage(message.username, permissionsSerialized))
            replyMes.onComplete {
              case Success(result) => {
                result.asInstanceOf[ReplyMessage].result match {
                  case EnumReplyResult.Done => {
                    selectedDatabase = ""
                    selectedMap = ""
                    reply(ReplyMessage(EnumReplyResult.Done, message), origSender)
                  }
                  case EnumReplyResult.Error => {
                    selectedDatabase = ""
                    selectedMap = ""
                    reply(ReplyMessage(EnumReplyResult.Error, message, new ReplyErrorInfo())) // should never get there
                  }
                }
              }
            }
          }
          case EnumReplyResult.Error => {
            selectedDatabase = ""
            selectedMap = ""
            reply(ReplyMessage(EnumReplyResult.Error, message, new NoKeysInfo()), origSender)
          }
        }
      }
      case Failure(t) => {
        log.error("Error sending message: " + t.getMessage);
        reply(new ReplyMessage(EnumReplyResult.Error, message,
          new ServiceErrorInfo("Error sending message: " + t.getMessage)), origSender)
      }
    }
  }

  /**
    * Processes SettingMessages messages.
    * Handles RefreshSettings message, reloading settings parameters from the configuration file
    *
    * @param message The SettingMessage message to process.
    * @see SettingMessages
    */
  private def handleSettingMessage(message: SettingMessage): Unit = {
    message match {
      // Refresh the settings from the conf file if requested
      case RefreshSettingsMessage() => {
        refreshStaticSetting()
        reply(new ReplyMessage(EnumReplyResult.Done, message, new RefreshSettingsInfo))
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handlePermissionsManagementMessage"))
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
    * @return <code>true</code> if the user has the permission to execute the query, <code>false</code> otherwise.
    * @see QueryMessage
    * @see PermissionMessages
    * @see ReadMessage
    * @see ReadWriteMessage
    * @see NoPermissionMessage
    */
  private def checkPermissions(message: QueryMessage, dbName: String): Boolean = {
    //TODO admin permissions

    // If permissions are null the user is a super admin and has every permissions
    if (perms == null)
      return true
    return message match {
      // If the message requires write permissions
      case n: ReadWriteMessage => {
        // Get the database permission
        val p = perms.get(dbName)
        // Return if the user has a write permissions
        p != null && p == EnumPermission.ReadWrite
      }
      // If the message requires read permissions
      case n: ReadMessage => {
        // Get the database permission
        val p = perms.get(dbName)
        // Return if the user has a read or write permissions
        p != null && (p == EnumPermission.Read || p == EnumPermission.ReadWrite)
      }
      // If the message doesn't require permissions
      case n: NoPermissionMessage => true
    }
  }

  /**
    * This method updates the Static settings values with the values return by ConfigurationManager.readActorProperties
    * which read the configuration file on disk. This method is being called when the admin changes that files and
    * afterwards type 'updatesettings' on console.
    */
  private def refreshStaticSetting(): Unit = {
    val confManager = new ConfigurationManager
    confManager.readActorsProperties()
  }
}
