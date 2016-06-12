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

package server.utils

import java.util

import server.enums.EnumPermission.UserPermission
import server.enums.EnumReplyResult
import server.enums.EnumReplyResult.Done

import scala.collection.JavaConversions._
import server.messages.query.PermissionMessages.{NoReadPermissionInfo, NoWritePermissionInfo}
import server.messages.query.admin.AdminMessage
import server.messages.query.admin.PermissionsManagementMessages._
import server.messages.query.admin.SettingsMessages.{RefreshSettingsInfo, RefreshSettingsMessage, SettingMessage}
import server.messages.query.admin.UsersManagementMessages._
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.HelpMessages._
import server.messages.query.user.MapMessages._
import server.messages.query.user.RowMessages._
import server.messages.query.user.UserMessage
import server.messages.query.{QueryMessage, ReplyInfo, ReplyMessage}

/**
  * Builds string replies from a ReplyMessage message.
  */
class ReplyBuilder {

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for UserMessage and AdminMessage messages.
    *
    * @param reply The ReplyMessage message.
    * @return The reply string.
    * @see ReplyMessage
    * @see UserMessage
    * @see AdminMessage
    */
  def buildReply(reply: ReplyMessage): String = {
    if (reply.info.isInstanceOf[server.messages.query.ReplyErrorInfo])
      "Unknown error, try again."
    else {
      reply.question match {
        case m: UserMessage => UserMessageReply(reply)
        case m: AdminMessage => AdminMessageReply(reply)
        case _ => "Unknown question " //TODO
      }
    }
  }

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for HelpMessage, DatabaseMessage, MapMessage and RowMessage messages.
    *
    * @param reply The ReplyMessage message.
    * @return The reply string.
    * @see ReplyMessage
    * @see HelpMessage
    * @see DatabaseMessage
    * @see MapMessage
    * @see RowMessage
    */
  private def UserMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case m: HelpMessage => HelpMessageReply(reply)
      case m: DatabaseMessage => DatabaseMessageReply(reply)
      case m: MapMessage => MapMessageReply(reply)
      case m: RowMessage => RowMessageReply(reply)
      case _ => "Unknown question UserMessage" //TODO
    }
  }

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for UsersManagementMessage, PermissionsManagementMessage, and ActorPropertiesMessage messages.
    *
    * @param reply The ReplyMessage message.
    * @return The reply string.
    * @see ReplyMessage
    * @see UsersManagementMessage
    * @see PermissionsManagementMessage
    * @see ActorPropertiesMessage
    */
  private def AdminMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case m: UsersManagementMessage => UserManagementMessageReply(reply)
      case m: PermissionsManagementMessage => PermissionsManagementMessageReply(reply)
      case m: SettingMessage => SettingMessagesReply(reply)
      case _ => "Unknown question AdminMessage" //TODO
    }
  }

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for ListUserMessage, AddUserMessage, and RemoveUserMessage messages.
    *
    * @param reply The ReplyMessage message.
    * @return The reply string.
    * @see ReplyMessage
    * @see ListUserMessage
    * @see AddUserMessage
    * @see RemoveUserMessage
    */
  private def UserManagementMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case ListUserMessage() => {
        reply.result match {
          case EnumReplyResult.Done => {
            val list : List[String] = reply.info.asInstanceOf[ListUserInfo].userList
            if ( list.size == 0 )
              return "No users at the moment, please insert some users."
            var toReturn : String = ""
            for (l <- list){
              toReturn = toReturn.concat(l + "\n")
            }
            toReturn = toReturn.dropRight(1)
            return toReturn
          }
          case EnumReplyResult.Error => {
            if (reply.info.isInstanceOf[NoUserInfo])
              return "No users, first add some users."
            else
              return "WTF?!? Should not reach that point"
          }
        }
      }
      case AddUserMessage(username: String, password: String) => return "User " + username + " has been added."
      case RemoveUserMessage(username: String) => return "User " + username + " has been removed."
      case _ => "" //TODO
    }
  }

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for ListPermissionMessage, AddPermissionMessage, and RemovePermissionMessage messages.
    *
    * @param reply The ReplyMessage message.
    * @return The reply string.
    * @see ReplyMessage
    * @see ListPermissionMessage
    * @see AddPermissionMessage
    * @see RemovePermissionMessage
    */
  private def PermissionsManagementMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case ListPermissionMessage(username: String) => {
        reply.result match {
          case Done => {
            reply.info match {
              case ListPermissionsInfo(map: util.HashMap[String, UserPermission]) => {
                var result: String = ""
                if (map.size() == 0)
                  return "User does not have any permission at the moment. Give him/her some permissions."
                for (k <- map.keySet()) {
                  result = result.concat(k)
                  result = result.concat(" -> ")
                  result = result.concat(map.get(k) + "\n")
                }
                return result.dropRight(1)
              }
            }
          }
          case EnumReplyResult.Error => {
            reply.info match {
              case NoKeyInfo() => "User does not exist."
            }
          }
        }
      }
      case AddPermissionMessage(username: String, database: String, permissionType: UserPermission) => {
        reply.result match {
          case EnumReplyResult.Done => {
            return "Permission " + permissionType.toString + " added to user " + username + " on database " + database
          }
          case EnumReplyResult.Error => {
            return "Something went wrong, try again."
          }
        }
      }
      case RemovePermissionMessage(username: String, database: String) => {
        return "Removed all permissions of " + username + " on DB " + database;
      }
      case _ => "" //TODO
    }
  }

  /**
    * Build string replies from a ReplyMessage message.
    * Handles reply for RefreshSettingsMessage message.
    *
    * @param reply the ReplyMessage message
    * @return The reply string.
    */
  private def SettingMessagesReply(reply: ReplyMessage) : String = {
    reply.question match {
      case RefreshSettingsMessage() => return "Permissions has been updated."
      case _ => "" //TODO
    }
  }

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for HelpMessages messages.
    *
    * @param reply The ReplyMessage message.
    * @return The reply string.
    * @see ReplyMessage
    */
  private def HelpMessageReply(reply: ReplyMessage): String = {
    reply.result match {
      case EnumReplyResult.Done => DoneHelpMessageReply(reply.question, reply.info)
      case EnumReplyResult.Error => "" //TODO
      case _ => "Unknown result on HelpMessage" //TODO
    }
  }

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for ListPermissionMessage, AddPermissionMessage, and RemovePermissionMessage messages.
    *
    * @param question The HelpMessage message question.
    * @param info The info of the ReplyMessage message.
    * @return The reply string.
    * @see QueryMessage
    * @see CompleteHelpReplyInfo
    * @see SpecificHelpMessage
    * @see CompleteHelpReplyInfo
    * @see SpecificHelpReplyInfo
    */
  private def DoneHelpMessageReply(question: QueryMessage, info: ReplyInfo): String = {
    question match {
      case CompleteHelpMessage() => {
        info match {
          case CompleteHelpReplyInfo(commands: String) => commands
          case _ => "Unknown done CompleteHelp info" //TODO
        }
      }
      case SpecificHelpMessage(command: String) => {
        info match {
          case SpecificHelpReplyInfo(command: String) => command
          case _ => "Unknown done SpecificHelp info" //TODO
        }
      }
    }
  }

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for ListDatabaseMessage, SelectDatabaseMessage, CreateDatabaseMessage and DeleteDatabaseMessage messages.
    *
    * @param reply The ReplyMessage message.
    * @return The reply string.
    * @see ReplyMessage
    * @see ListDatabaseMessage
    * @see SelectDatabaseMessage
    * @see CreateDatabaseMessage
    * @see DeleteDatabaseMessage
    * @see NoDBInfo
    * @see NoReadPermissionInfo    *
    * @see NoWritePermissionInfo
    * @see DBDoesNotExistInfo
    * @see DBAlreadyExistInfo
    */
  private def DatabaseMessageReply(reply: ReplyMessage): String = {
    reply.result match {
      // db message question done
      case EnumReplyResult.Done => {
        reply.question match {
          case ListDatabaseMessage() => {
            //info for a done ListDatabaseMessage
            reply.info match {
              case ListDBInfo(dbs: List[String]) => dbs.mkString("\n")
              case _ => "Unknown info on done ListDatabaseMessage" //TODO
            }
          }
          case SelectDatabaseMessage(name: String) => "Database " + name + " selected"
          case CreateDatabaseMessage(name: String) => "Database " + name + " created"
          case DeleteDatabaseMessage(name: String) => "Database " + name + " deleted"
          case _ => "Unknown question on done db message reply" //TODO
        }
      }
      // db message question not done or anomaly done
      case EnumReplyResult.Error => {
        reply.question match {
          case ListDatabaseMessage() => {
            reply.info match {
              //info for an error ListDatabaseMessage
              case NoDBInfo() => "No database found"
              case _ => "Unknown info on error ListDatabaseMessage" //TODO
            }
          }
          case SelectDatabaseMessage(name: String) => {
            reply.info match {
              //info for an error SelectDatabaseMessage
              case DBDoesNotExistInfo() => "Database " + name + " not exist"
              case NoReadPermissionInfo() => "No read permission"
              case _ => "Unknown info on error SelectDatabaseMessage" //TODO
            }
          }
          case CreateDatabaseMessage(name: String) => {
            reply.info match {
              //info for an error CreateDatabaseMessage
              case DBAlreadyExistInfo() => "The database name " + name + " is already used"
              case _ => "Unknown info on error CreateDatabaseMessage" //TODO
            }
          }
          case DeleteDatabaseMessage(name: String) => {
            reply.info match {
              //info for an error DeleteDatabaseMessage
              case NoWritePermissionInfo() => "No write permission "
              case DBDoesNotExistInfo() => "Database " + name + " not exist"
              case _ => "Unknown info on error DeleteDatabaseMessage" //TODO
            }
          }
          case _ => "Unknown question on error db message reply" //TODO
        }
      }
      case _ => "Unknown result on db message reply " //TODO
    }
  }

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for ListMapMessage, SelectMapMessage, CreateMapMessage and DeleteMapMessage messages.
    *
    * @param reply The ReplyMessage message.
    * @return The reply string.
    * @see ReplyMessage
    * @see ListMapMessage
    * @see SelectMapMessage
    * @see CreateMapMessage
    * @see DeleteMapMessage
    * @see NoMapInfo
    * @see NoDBSelectedInfo
    * @see NoReadPermissionInfo    *
    * @see NoWritePermissionInfo
    * @see MapDoesNotExistInfo
    * @see MapAlreadyExistInfo
    */
  private def MapMessageReply(reply: ReplyMessage): String = {
    reply.result match {
      //question was done
      case EnumReplyResult.Done => {
        reply.question match {
          case ListMapMessage() => {
            reply.info match {
              //info for a done ListMapMessage
              case ListMapInfo(dbs: List[String]) => dbs.mkString("\n")
              case _ => "Unknown info for a done ListMapMessage" //TODO
            }
          }
          case SelectMapMessage(name: String) => "Map " + name + " selected"
          case CreateMapMessage(name: String) => "Map " + name + " created"
          case DeleteMapMessage(name: String) => "Map " + name + " deleted"
          case _ => "Question done map message unknown" //TODO
        }
      }
      //the question wasn't normally done
      case EnumReplyResult.Error => {
        //check if db wasn't selected error
        if (reply.info.isInstanceOf[NoDBSelectedInfo]) {
          return "Select database first"
        }
        //check if there was a read permission error
        if (reply.info.isInstanceOf[NoReadPermissionInfo]) {
          return "No read permission"
        }
        reply.question match {
          case ListMapMessage() => {
            reply.info match {
              //info for a error ListMapMessage
              case NoMapInfo() => "No map found"
              case _ => "Unknown info for a error ListMapMessage" //TODO
            }
          }
          case SelectMapMessage(name: String) => {
            reply.info match {
              //info for a error SelectMapMessage
              case MapDoesNotExistInfo() => "Map " + name + " not exist"
              case _ => "Unknown info for a error ListMapMessage" //TODO
            }
          }
          case CreateMapMessage(name: String) => {
            reply.info match {
              //info for a error CreateMapMessage
              case MapAlreadyExistInfo() => "The map name " + name + " is already used"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for a error CreateMapMessage" //TODO
            }
          }
          case DeleteMapMessage(name: String) => {
            reply.info match {
              //info for a error DeleteMapMessage
              case MapDoesNotExistInfo() => "Map " + name + " not exist"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for a error DeleteMapMessage" //TODO
            }
          }
          case _ => "Unknown question on error map message reply" //TODO
        }
      }
      case _ => "Unknown result on map message reply" //TODO
    }
  }

  /**
    * Builds string replies from a ReplyMessage message.
    * Handles reply for ListMapMessage, SelectMapMessage, CreateMapMessage and DeleteMapMessage messages.
    *
    * @param reply The ReplyMessage message.
    * @return The reply string.
    * @see ReplyMessage
    * @see ListMapMessage
    * @see SelectMapMessage
    * @see CreateMapMessage
    * @see DeleteMapMessage
    * @see NoMapInfo
    * @see NoDBSelectedInfo
    * @see NoReadPermissionInfo    *
    * @see NoWritePermissionInfo
    * @see MapDoesNotExistInfo
    * @see MapAlreadyExistInfo
    */
  private def RowMessageReply(reply: ReplyMessage): String = {
    reply.result match {
      //the question was correctly done
      case EnumReplyResult.Done => {
        reply.question match {
          case ListKeysMessage() => {
            //possible info for a done ListKeysMessage
            reply.info match {
              case ListKeyInfo(dbs: List[String]) => dbs.mkString("\n")
              case _ => "Unknown info for ListKeysMessage done" //TODO
            }
          }
          case FindRowMessage(key: String) => {
            reply.info match {
              //possible info for a done FindRowMessage
              case FindInfo(value: Array[Byte]) => new String(value, "UTF-8")
              case _ => "Unknown info for FindRowMessage done" //TODO
            }
          }
          case InsertRowMessage(key: String, value: Array[Byte]) => "Key " + key + " with value " + new String(value, "UTF-8") + " inserted"
          case UpdateRowMessage(key: String, value: Array[Byte]) => "Key " + key + " updated with value " + new String(value, "UTF-8")
          case RemoveRowMessage(key: String) => "Key " + key + " deleted"
          case _ => "Unknown done row message question " //TODO
        }
      }
      //the question wasn't normally done
      case EnumReplyResult.Error => {
        //check if db wasn't selected error
        if (reply.info.isInstanceOf[NoDBSelectedInfo]) {
          return "Select database and map first"
        }
        //check if map wasn't selected error
        if (reply.info.isInstanceOf[NoMapSelectedInfo]) {
          return "Select map first"
        }
        //check if there was a read permission error
        if (reply.info.isInstanceOf[NoReadPermissionInfo]) {
          return "No read permission"
        }
        reply.question match {
          case ListKeysMessage() => {
            //possible info for a failed ListKeysMessage
            reply.info match {
              case NoKeyInfo() => "No key found"
              case _ => "Unknown info for ListKeysMessage error" //TODO
            }
          }
          case FindRowMessage(key: String) => {
            //possible info for a failed FindRowMessage
            reply.info match {
              case KeyDoesNotExistInfo() => "Key " + key + " not exist"
              case _ => "Unknown info for FindRowMessage error" //TODO
            }
          }
          case InsertRowMessage(key: String, value: Array[Byte]) => {
            reply.info match {
              //possible info for a failed InsertRowMessage
              case KeyAlreadyExistInfo() => "Key " + key + " is already used"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for InsertRowMessage  error" //TODO
            }
          }
          case UpdateRowMessage(key: String, value: Array[Byte]) => {
            reply.info match {
              //possible info for a failed UpdateRowMessage
              case KeyDoesNotExistInfo() => "Key " + key + " not exist"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for UpdateRowMessage error" //TODO
            }
          }
          case RemoveRowMessage(key: String) => {
            reply.info match {
              //possible info for a failed RemoveRowMessage
              case KeyDoesNotExistInfo() => "Key " + key + " not exist"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for RemoveRowMessage error" //TODO
            }
          }
          case _ => "Unknown question on error row message reply" //TODO
        }
      }
      case _ => "Unknown result on row message reply" //TODO
    }
  }

  /**
    * Returns an 'Unhandled message' reply stirng.
    *
    * @param actor The actor path.
    * @param method The method name.
    * @return The 'unhandled message' string.
    */
  def unhandledMessage(actor: String, method: String): String = {
    "Unhandled message in actor; " + actor + ", method: " + method
  }
}