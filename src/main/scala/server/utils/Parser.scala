package server.utils

import akka.event.LoggingAdapter
import server.enums.EnumPermission
import server.messages.query.ErrorMessages.InvalidQueryMessage
import server.messages.query.HelpMessages.{CompleteHelp, SpecificHelp}
import server.messages.query.admin.PermissionsManagementMessages.{AddPermissionMessage, ListPermissionMessage, RemovePermissionMessage}
import server.messages.query.admin.UsersManagementMessages.{AddUserMessage, ListUserMessage, RemoveUserMessage}
import server.messages.query.{ErrorMessages, LoginMessage, QueryMessage}
import server.messages.query.user.DatabaseMessages.{CreateDatabaseMessage, DeleteDatabaseMessage, ListDatabaseMessage, SelectDatabaseMessage}
import server.messages.query.user.MapMessages.{CreateMapMessage, DeleteMapMessage, ListMapMessage, SelectMapMessage}
import server.messages.query.user.RowMessages._

import scala.util.matching.Regex

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
class Parser {

  def parseQuery(query: String, log:LoggingAdapter = null) : QueryMessage = {
    // Connect command
    var pattern = "login\\s(\\S+)\\s(\\S+)$".r
    var m = getMatch(pattern, query)
    if(m != null) return new LoginMessage(m.group(1), m.group(2))

    // Row command (two parameters)
    pattern = "(\\S+)\\s\\'(.+)\\'\\s(\\S+)$".r
    m = getMatch(pattern, query)
    if(m != null) return parseRowCommandTwoParams(m.group(1).toLowerCase(), m.group(2), m.group(3))

    // Row command (one parameter)
    pattern = "(\\S+)\\s\\'(.+)\\'$".r
    m = getMatch(pattern, query)
    if(m != null) return parseRowCommandOneParam(m.group(1).toLowerCase(), m.group(2))

    // Command with three params
    pattern = "(\\S+)\\s(\\S+)\\s(\\S+)\\s(\\S+)$".r
    m = getMatch(pattern, query)
    if(m != null) return parseCommandWithThreeParams(m.group(1).toLowerCase(), m.group(2), m.group(3), m.group(4))

    // Command with two params
    pattern = "(\\S+)\\s(\\S+)\\s(\\S+)$".r
    m = getMatch(pattern, query)
    if(m != null) return parseCommandWithTwoParams(m.group(1).toLowerCase(), m.group(2), m.group(3))

    // Command with parameters
    pattern = "(\\S+)\\s(\\S+)$".r
    m = getMatch(pattern, query)
    if(m != null) return parseCommandWithParam(m.group(1).toLowerCase(), m.group(2))

    // Command without parameters
    pattern = "(\\S+)$".r
    m = getMatch(pattern, query)
    if(m != null) return parseCommandWithoutParam(m.group(1).toLowerCase())

    return new InvalidQueryMessage
  }

  /** Finds pattern matches on the command */
  private def getMatch(pattern:Regex, command:String): Regex.Match = {
    val result = pattern.findFirstMatchIn(command)
    if (result.isDefined) return result.get
    return null
  }


  /** Parses commands without parameters */
  private def parseCommandWithoutParam(command: String): QueryMessage = {
    //renamed due to query without params for row level
    command match {
      case "listuser" => return new ListUserMessage
      case "listdb" => return new ListDatabaseMessage
      case "listmap" => return new ListMapMessage
      case "keys" => return new ListKeysMessage
      case "help" => return new CompleteHelp

      case _ => return new InvalidQueryMessage
    }
  }

  /** Parses commands with parameters */
  private def parseCommandWithParam(command: String, arg: String): QueryMessage = {
    command match {
      case "selectdb" => return new SelectDatabaseMessage(arg)
      case "createdb" => return new CreateDatabaseMessage(arg)
      case "deletedb" => return new DeleteDatabaseMessage(arg)

      case "selectmap" => return new SelectMapMessage(arg)
      case "createmap" => return new CreateMapMessage(arg)
      case "deletemap" => return new DeleteMapMessage(arg)

      case "removeuser" => return new RemoveUserMessage(arg)
      case "listpermission" => return new ListPermissionMessage(arg)

      case "help" => return new SpecificHelp(arg)

      case _ => return new InvalidQueryMessage
    }
  }

  private def parseCommandWithTwoParams(command:String, arg1: String, arg2: String):QueryMessage = {
    command match {
      case "adduser" => return new AddUserMessage(arg1, arg2)
      case "removepermission" => return new RemovePermissionMessage(arg1, arg2)

      case _ => new InvalidQueryMessage
    }
  }

  private def parseCommandWithThreeParams(command:String, arg1: String, arg2: String, arg3: String):QueryMessage = {
    command match {
      case "addpermission" => {
        arg3 match {
          case "read" => return new AddPermissionMessage(arg1, arg2, EnumPermission.Read)
          case "readwrite" => return new AddPermissionMessage(arg1, arg2, EnumPermission.ReadWrite)
          case _ => return new InvalidQueryMessage
        }
      }

      case _ => return new InvalidQueryMessage
    }
  }

  /** Parses row level commands with one parameter */
  private def parseRowCommandOneParam(command: String, key: String): QueryMessage = {
    command match {
      case "find" =>return new FindRowMessage(key)
      case "remove" => return new RemoveRowMessage(key)

      case _ => return new InvalidQueryMessage
    }
  }

  /** Parses row level commands with two parameters */
  private def parseRowCommandTwoParams(command: String, key: String, value: String): QueryMessage = {
    command match {
      case "insert" => return new InsertRowMessage(key, value.getBytes("UTF-8"))
      case "update" => return new UpdateRowMessage(key, value.getBytes("UTF-8"))

      case _ => new InvalidQueryMessage
    }
  }
}