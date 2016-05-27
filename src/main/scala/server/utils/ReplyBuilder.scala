package server.utils

import server.enums.EnumPermission.UserPermission
import server.enums.EnumReplyResult
import server.messages.query.HelpMessages._
import server.messages.query.PermissionMessages.{NoWritePermissionInfo, NoReadPermissionInfo}
import server.messages.query.admin.ActorPropetiesMessages.ActorPropertiesMessage
import server.messages.query.admin.AdminMessage
import server.messages.query.admin.PermissionsManagementMessages.{RemovePermissionMessage, AddPermissionMessage, ListPermissionMessage, PermissionsManagementMessage}
import server.messages.query.admin.UsersManagementMessages.{RemoveUserMessage, AddUserMessage, ListUserMessage, UsersManagementMessage}
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.MapMessages._
import server.messages.query.user.RowMessages._
import server.messages.query.user.UserMessage
import server.messages.query.{ReplyInfo, QueryMessage, ReplyMessage}


/**
  * Created by borto on 26/05/2016.
  */
class ReplyBuilder {
  /** transform a ReplyMessage into a String */
  def buildReply(reply: ReplyMessage) : String = {
    reply.question match {
      case m: UserMessage => UserMessageReply(reply)
      case m: AdminMessage => AdminMessageReply(reply)
      case _ => "Unknown question "//TODO
    }
  }

  /**create reply String from UserMessage  */
  private def UserMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case m: HelpMessage => HelpMessageReply(reply)
      case m: DatabaseMessage => DatabaseMessageReply(reply)
      case m: MapMessage => MapMessageReply(reply)
      case m: RowMessage => RowMessageReply(reply)
      case _ =>"Unknown question UserMessage" //TODO
    }
  }

  /** create reply String from admin query messages (UserManagement, PermissionManagement and Properties) */
  private def AdminMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case m:UsersManagementMessage => UserManagementMessageReply(reply)
      case m:PermissionsManagementMessage => PermissionsManagementMessageReply(reply)
      case m:ActorPropertiesMessage =>  ActorPropertiesMessageMessageReply(reply)
      case _ => "Unknown question AdminMessage"//TODO
    }
  }

  /** create reply String from user management messages (ListUser, AddUser and RemoveUser) */
  private def UserManagementMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case ListUserMessage() =>"" //TODO
      case AddUserMessage(username: String, password:String) => ""//TODO
      case RemoveUserMessage(username: String) => ""//TODO
      case _ =>"" //TODO
    }
  }

  /** create reply String from permission management messages (ListPermission, AddPermission, RemovePermission) */
  private def PermissionsManagementMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case ListPermissionMessage(username:String) =>"" //TODO
      case AddPermissionMessage(username: String, database:String, permissionType: UserPermission) =>"" //TODO
      case RemovePermissionMessage(username: String, database:String) =>"" //TODO
      case _ =>"" //TODO
    }
  }

  /** create reply String from actors' properties messages */
  private def ActorPropertiesMessageMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case _ =>"" //TODO
    }
  }

  /** create String from help messages (complete or specific ones) */
  private def HelpMessageReply(reply: ReplyMessage): String = {
    reply.result match {
      case EnumReplyResult.Done => DoneHelpMessageReply(reply.question,reply.info)
      case EnumReplyResult.Error => "" //TODO
      case _ => "Unknown result on HelpMessage" //TODO
    }
  }
  /**create final string for Done Help*/
  private def DoneHelpMessageReply(question:QueryMessage,info:ReplyInfo): String ={
    question match{
      case CompleteHelp() => {
        info match {
          case CompleteHelpReplyInfo(commands: String) => commands
          case _ => "Unknown done CompleteHelp info" //TODO
        }
      }
      case SpecificHelp(command: String) =>{
        info match {
          case SpecificHelpReplyInfo(command: String) => command
          case _ => "Unknown done SpecificHelp info" //TODO
        }
      }
    }
  }
  /** create reply String for database messages (ListDatabase, SelectDatabase, CreateDatabase and DeleteDatabase) */
  private def DatabaseMessageReply(reply: ReplyMessage): String = {
    reply.result match {
        // db message question done
      case EnumReplyResult.Done =>{
        reply.question match{
          case ListDatabaseMessage() => {
            //info for a done ListDatabaseMessage
            reply.info match {
              case ListDBInfo(dbs: List[String]) => dbs.mkString("/n").dropRight(1)
              case _ => "Unknown info on done ListDatabaseMessage" //TODO
            }
          }
          case SelectDatabaseMessage(name: String) => "Database "+name+" selected"
          case CreateDatabaseMessage(name: String) => "Database "+name+" created"
          case DeleteDatabaseMessage(name: String) => "Database "+name+" deleted"
          case _ =>"Unknown question on done db message reply" //TODO
        }
      }
        // db message question not done or anomaly done
      case EnumReplyResult.Error =>{
        reply.question match{
          case ListDatabaseMessage() => {
            reply.info match {
              //info for an error ListDatabaseMessage
              case NoDBInfo() => "No database found"
              case _ => "Unknown info on error ListDatabaseMessage"//TODO
            }
          }
          case SelectDatabaseMessage(name: String) =>{
            reply.info match {
              //info for an error SelectDatabaseMessage
              case DBDoesNotExistInfo() => "Database "+name+" not exist"
              case NoReadPermissionInfo() => "No read permission"
              case _ => "Unknown info on error SelectDatabaseMessage"//TODO
            }
          }
          case CreateDatabaseMessage(name: String) => {
            reply.info match {
              //info for an error CreateDatabaseMessage
              case DBAlreadyExistInfo() => "The database name "+name+" is already used"
              case _ => "Unknown info on error CreateDatabaseMessage"//TODO
            }
          }
          case DeleteDatabaseMessage(name: String) => {
            reply.info match {
                //info for an error DeleteDatabaseMessage
              case NoWritePermissionInfo() => "No write permission "
              case DBDoesNotExistInfo() => "Database "+name+" not exist"
              case _ => "Unknown info on error DeleteDatabaseMessage"//TODO
            }
          }
          case _ => "Unknown question on error db message reply"//TODO
        }
      }
      case _ => "Unknown result on db message reply "//TODO
    }
  }

  /** create reply string for map messages */
  private def MapMessageReply(reply: ReplyMessage): String = {
    reply.result match {
      //question was done
      case EnumReplyResult.Done =>{
        reply.question match{
          case ListMapMessage() => {
            reply.info match {
                //info for a done ListMapMessage
              case ListMapInfo(dbs: List[String]) => dbs.mkString("/n").dropRight(1)
              case _ => "Unknown info for a done ListMapMessage" //TODO
            }
          }
          case SelectMapMessage(name: String) => "Map "+name+" selected"
          case CreateMapMessage(name: String) => "Map "+name+" created"
          case DeleteMapMessage(name: String) => "Map "+name+" deleted"
          case _ =>"Question done map message unknown" //TODO
        }
      }
      //the question wasn't normally done
      case EnumReplyResult.Error =>{
        //check if db wasn't selected error
        if(reply.info.isInstanceOf[NoDBSelectedInfo]){
          return "Select database first"
        }
        //check if there was a read permission error
        if(reply.info.isInstanceOf[NoReadPermissionInfo]){
          return "No read permission"
        }
        reply.question match{
          case ListMapMessage() => {
            reply.info match {
              //info for a error ListMapMessage
              case NoMapInfo() => "No map found"
              case _ => "Unknown info for a error ListMapMessage"//TODO
            }
          }
          case SelectMapMessage(name: String) =>{
            reply.info match {
              //info for a error SelectMapMessage
              case MapDoesNotExistInfo() => "Map "+name+" not exist"
              case _ => "Unknown info for a error ListMapMessage"//TODO
            }
          }
          case CreateMapMessage(name: String) => {
            reply.info match {
              //info for a error CreateMapMessage
              case MapAlreadyExistInfo() => "The map name "+name+" is already used"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for a error CreateMapMessage"//TODO
            }
          }
          case DeleteMapMessage(name: String) => {
            reply.info match {
              //info for a error DeleteMapMessage
              case MapDoesNotExistInfo() => "Map "+name+" not exist"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for a error DeleteMapMessage"//TODO
            }
          }
          case _ => "Unknown question on error map message reply"//TODO
        }
      }
      case _ => "Unknown result on map message reply"//TODO
    }
  }

  /** create reply string for row messages */
  private def RowMessageReply(reply: ReplyMessage): String = {
    reply.result match {
        //the question was correctly done
      case EnumReplyResult.Done =>{
        reply.question match{
          case ListKeysMessage() => {
            //possible info for a done ListKeysMessage
            reply.info match {
              case ListKeyInfo(dbs: List[String]) => dbs.mkString("/n").dropRight(1)
              case _ => "Unknown info for ListKeysMessage done" //TODO
            }
          }
          case FindRowMessage(key: String) =>{
            reply.info match {
              //possible info for a done FindRowMessage
              case FindInfo(value: String) => value
              case _ => "Unknown info for FindRowMessage done" //TODO
            }
          }
          case InsertRowMessage(key: String, value: String) => "Key "+key+" with value "+value+" inserted"
          case UpdateRowMessage(key: String, value: String) => "Key "+key+" updated with value "+value
          case RemoveRowMessage(key: String) => "Key "+key+" deleted"
          case _ =>"Unknown done row message question " //TODO
        }
      }
        //the question wasn't normally done
      case EnumReplyResult.Error =>{
        //check if db wasn't selected error
        if(reply.info.isInstanceOf[NoDBSelectedInfo]){
          return "Select database and map first"
        }
        //check if map wasn't selected error
        if(reply.info.isInstanceOf[NoMapSelectedInfo]){
          return "Select map first"
        }
        //check if there was a read permission error
        if(reply.info.isInstanceOf[NoReadPermissionInfo]){
          return "No read permission"
        }
        reply.question match{
          case ListKeysMessage() => {
            //possible info for a failed ListKeysMessage
            reply.info match {
              case NoKeyInfo() => "No key found"
              case _ => "Unknown info for ListKeysMessage error"//TODO
            }
          }
          case FindRowMessage(key: String) =>{
            //possible info for a failed FindRowMessage
            reply.info match {
              case KeyDoesNotExistInfo() => "Key "+key+" not exist"
              case _ => "Unknown info for FindRowMessage error"//TODO
            }
          }
          case InsertRowMessage(key: String, value: String) => {
            reply.info match {
              //possible info for a failed InsertRowMessage
              case KeyAlreadyExistInfo() => "Key "+key+" is already used"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for InsertRowMessage  error"//TODO
            }
          }
          case UpdateRowMessage(key: String, value: String) => {
            reply.info match {
              //possible info for a failed UpdateRowMessage
              case  KeyDoesNotExistInfo() => "Key "+key+" not exist"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for UpdateRowMessage error"//TODO
            }
          }
          case RemoveRowMessage(key: String) => {
            reply.info match {
              //possible info for a failed RemoveRowMessage
              case  KeyDoesNotExistInfo() => "Key "+key+" not exist"
              case NoWritePermissionInfo() => "No write permission"
              case _ => "Unknown info for RemoveRowMessage error"//TODO
            }
          }
          case _ => "Unknown question on error row message reply"//TODO
        }
      }
      case _ => "Unknown result on row message reply"//TODO
    }
  }

  def unhandledMessage(actor: String, method: String) : String = {
    "Unhandled message in actor; " + actor + ", method: " + method
  }
}