package server.utils

import akka.actor.Props
import server.Server
import server.actors.Storemanager
import server.enums.EnumPermission.UserPermission
import server.enums.EnumReplyResult
import server.messages.internal.AskMapMessage
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

import scala.util.{Failure, Success}

/**
  * Created by borto on 26/05/2016.
  */
class ReplyBuilder {
  /** transform a ReplyMessage into a String */
  def buildReply(reply: ReplyMessage) : String = {
    reply.question match {
      case m: UserMessage => UserMessageReply(reply)
      case m: AdminMessage => AdminMessageReply(reply)
      case _ => ""//TODO
    }
  }

  /**create reply String from UserMessage  */
  private def UserMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case m: HelpMessage => HelpMessageReply(reply)
      case m: DatabaseMessage => DatabaseMessageReply(reply)
      case m: MapMessage => MapMessageReply(reply)
      case m: RowMessage => RowMessageReply(reply)
      case _ =>"" //TODO
    }
  }

  /** create reply String from admin query messages (UserManagement, PermissionManagement and Properties) */
  private def AdminMessageReply(reply: ReplyMessage): String = {
    reply.question match {
      case m:UsersManagementMessage => UserManagementMessageReply(reply)
      case m:PermissionsManagementMessage => PermissionsManagementMessageReply(reply)
      case m:ActorPropertiesMessage =>  ActorPropertiesMessageMessageReply(reply)
      case _ => ""//TODO
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
      case _ => "" //TODO
    }
  }
  /**create final string for Done Help*/
  private def DoneHelpMessageReply(question:QueryMessage,info:ReplyInfo): String ={
    question match{
      case CompleteHelp() => {
        info match {
          case CompleteHelpReplyInfo(commands: String) => commands
          case _ => "" //TODO
        }
      }
      case SpecificHelp(command: String) =>{
        info match {
          case SpecificHelpReplyInfo(command: String) => command
          case _ => "" //TODO
        }
      }
    }
  }
  /** create reply String for database messages (ListDatabase, SelectDatabase, CreateDatabase and DeleteDatabase) */
  private def DatabaseMessageReply(reply: ReplyMessage): String = {
    reply.result match {
      case EnumReplyResult.Done =>{
        reply.question match{
          case ListDatabaseMessage() => {
            reply.info match {
              case ListDBInfo(dbs: List[String]) => dbs.mkString("/n").dropRight(1)
              case _ => "" //TODO
            }
          }
          case SelectDatabaseMessage(name: String) => "Database "+name+" selected"
          case CreateDatabaseMessage(name: String) => "Database "+name+" created"
          case DeleteDatabaseMessage(name: String) => "Database "+name+" deleted"
          case _ =>"" //TODO
        }
      }
      case EnumReplyResult.Error =>{
        reply.question match{
          case ListDatabaseMessage() => {
            reply.info match {
              case NoDBInfo() => "No database found"
              case _ => ""//TODO
            }
          }
          case SelectDatabaseMessage(name: String) =>{
            reply.info match {
              case DBDoesNotExistInfo() => "Database "+name+" not exist"
              case _ => ""//TODO
            }
          }
          case CreateDatabaseMessage(name: String) => {
            reply.info match {
              case DBAlreadyExistInfo() => "The database name: "+name+" is already used"
              case _ => ""//TODO
            }
          }
          case DeleteDatabaseMessage(name: String) => {
            reply.info match {
              case DBDoesNotExistInfo() => "Database "+name+" not exist"
              case _ => ""//TODO
            }
          }
          case _ => ""//TODO
        }
      }
      case _ => ""//TODO
    }
  }

  /** create reply string for map messages */
  private def MapMessageReply(reply: ReplyMessage): String = {
    reply.result match {
      case EnumReplyResult.Done =>{
        reply.question match{
          case ListMapMessage() => {
            reply.info match {
              case ListMapInfo(dbs: List[String]) => dbs.mkString("/n").dropRight(1)
              case _ => "" //TODO
            }
          }
          case SelectMapMessage(name: String) => "Map "+name+" selected"
          case CreateMapMessage(name: String) => "Map "+name+" created"
          case DeleteMapMessage(name: String) => "Map "+name+" deleted"
          case _ =>"" //TODO
        }
      }
      case EnumReplyResult.Error =>{
        if(reply.info.isInstanceOf[NoDBSelectedInfo]){
          return "Select database first"
        }
        reply.question match{
          case ListMapMessage() => {
            reply.info match {
              case NoMapInfo() => "No map found"
              case _ => ""//TODO
            }
          }
          case SelectMapMessage(name: String) =>{
            reply.info match {
              case MapDoesNotExistInfo() => "Map "+name+" not exist"
              case _ => ""//TODO
            }
          }
          case CreateMapMessage(name: String) => {
            reply.info match {
              case MapAlreadyExistInfo() => "The map name: "+name+" is already used"
              case _ => ""//TODO
            }
          }
          case DeleteMapMessage(name: String) => {
            reply.info match {
              case MapDoesNotExistInfo() => "Map "+name+" not exist"
              case _ => ""//TODO
            }
          }
          case _ => ""//TODO
        }
      }
      case _ => ""//TODO
    }
  }

  /** Manages row messages */
  private def RowMessageReply(reply: ReplyMessage): String = {
    reply.result match {
      case EnumReplyResult.Done =>{
        reply.question match{
          case ListKeysMessage() => {
            reply.info match {
              case ListKeyInfo(dbs: List[String]) => dbs.mkString("/n").dropRight(1)
              case _ => "" //TODO
            }
          }
          case FindRowMessage(key: String) =>{
            reply.info match {
              case FindInfo(value: String) => value
              case _ => "" //TODO
            }
          }
          case InsertRowMessage(key: String, value: String) => "Key: "+key+" with value: "+value+" inserted"
          case UpdateRowMessage(key: String, value: String) => "Key: "+key+" updated with value: "+value
          case RemoveRowMessage(key: String) => "Key: "+key+" deleted"
          case _ =>"" //TODO
        }
      }
      case EnumReplyResult.Error =>{
        if(reply.info.isInstanceOf[NoDBSelectedInfo]){
          return "Select database and map first"
        }
        if(reply.info.isInstanceOf[NoMapSelectedInfo]){
          return "Select map first"
        }
        reply.question match{
          case ListKeysMessage() => {
            reply.info match {
              case NoKeyInfo() => "No key found"
              case _ => ""//TODO
            }
          }
          case FindRowMessage(key: String) =>{
            reply.info match {
              case KeyDoesNotExistInfo() => "Key: "+key+" not exist"
              case _ => ""//TODO
            }
          }
          case InsertRowMessage(key: String, value: String) => {
            reply.info match {
              case KeyAlreadyExistInfo() => "Key: "+key+" is already used"
              case _ => ""//TODO
            }
          }
          case UpdateRowMessage(key: String, value: String) => {
            reply.info match {
              case  KeyDoesNotExistInfo() => "Key: "+key+" not exist"
              case _ => ""//TODO
            }
          }
          case RemoveRowMessage(key: String) => {
            reply.info match {
              case  KeyDoesNotExistInfo() => "Key: "+key+" not exist"
              case _ => ""//TODO
            }
          }
          case _ => ""//TODO
        }
      }
      case _ => ""//TODO
    }
  }

  def unhandledMessage(actor: String, method: String) : String = {
    "Unhandled message in actor; " + actor + ", method: " + method
  }
}
