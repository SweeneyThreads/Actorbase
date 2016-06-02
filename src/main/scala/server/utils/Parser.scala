package server.utils

import server.enums.EnumPermission
import server.messages.query.ErrorMessages.InvalidQueryMessage
import server.messages.query.user.HelpMessages.{CompleteHelpMessage, SpecificHelpMessage}
import server.messages.query.admin.PermissionsManagementMessages.{AddPermissionMessage, ListPermissionMessage, RemovePermissionMessage}
import server.messages.query.admin.UsersManagementMessages.{AddUserMessage, ListUserMessage, RemoveUserMessage}
import server.messages.query.user.DatabaseMessages.{CreateDatabaseMessage, DeleteDatabaseMessage, ListDatabaseMessage, SelectDatabaseMessage}
import server.messages.query.user.MapMessages.{CreateMapMessage, DeleteMapMessage, ListMapMessage, SelectMapMessage}
import server.messages.query.user.RowMessages._
import server.messages.query.{LoginMessage, QueryMessage}

import scala.util.matching.Regex

/**
  * Created by matteobortolazzo on 02/05/2016.
  * Parses user's requests into QueryMessage messages.
  */
class Parser {

  /**
    * Parses the query string into a QueryMessage.
    *
    * @param query The string query.
    * @return The message representing the user's query
    */
  def parseQuery(query: String) : QueryMessage = {

    // Row command (two parameters)
    var pattern = "(\\S+)\\s\\'(.+)\\'\\s(\\S+)$".r
    var m = getMatch(pattern, query)
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

    new InvalidQueryMessage
  }

  /**
    * Returns the regular expression match of the given string.
    *
    * @param pattern The regular expression for the check.
    * @param command The command to check.
    * @return
    */
  private def getMatch(pattern:Regex, command:String): Regex.Match = {
    val result = pattern.findFirstMatchIn(command)
    if (result.isDefined)
      result.get
    else null
  }


  /**
    * Parses commands without any parameters.
    *
    * @param command The command to check.
    * @return The QueryMessage message
    *
    * @see ListUserMessage
    * @see ListDatabaseMessage
    * @see ListMapMessage
    * @see ListKeysMessage
    * @see CompleteHelpMessage
    * @see InvalidQueryMessage
    */
  private def parseCommandWithoutParam(command: String): QueryMessage = {
    //renamed due to query without params for row level
    command match {
      case "listuser" =>  new ListUserMessage
      case "listdb" =>  new ListDatabaseMessage
      case "listmap" =>  new ListMapMessage
      case "keys" =>  new ListKeysMessage
      case "help" =>  new CompleteHelpMessage

      case _ =>  new InvalidQueryMessage
    }
  }

  /**
    * Parses commands with one parameter.
    *
    * @param command The command to check.
    * @param arg The fist parameter.
    * @return The QueryMessage message
    *
    * @see SelectDatabaseMessage
    * @see CreateDatabaseMessage
    * @see DeleteDatabaseMessage
    * @see SelectMapMessage
    * @see CreateMapMessage
    * @see DeleteMapMessage
    * @see RemoveUserMessage
    * @see ListPermissionMessage
    * @see SpecificHelpMessage
    * @see InvalidQueryMessage
    */
  private def parseCommandWithParam(command: String, arg: String): QueryMessage = {
    command match {
      case "selectdb" =>  new SelectDatabaseMessage(arg)
      case "createdb" =>  new CreateDatabaseMessage(arg)
      case "deletedb" =>  new DeleteDatabaseMessage(arg)

      case "selectmap" =>  new SelectMapMessage(arg)
      case "createmap" =>  new CreateMapMessage(arg)
      case "deletemap" =>  new DeleteMapMessage(arg)

      case "removeuser" =>  new RemoveUserMessage(arg)
      case "listpermission" =>  new ListPermissionMessage(arg)

      case "help" =>  new SpecificHelpMessage(arg)

      case _ =>  new InvalidQueryMessage
    }
  }

  /**
    * Parses commands with two parameters.
    *
    * @param command The command to check.
    * @param arg1 The fist parameter.
    * @param arg2 The second parameter.
    * @return The QueryMessage message.
    *
    * @see AddUserMessage
    * @see RemovePermissionMessage
    * @see InvalidQueryMessage
    */
  private def parseCommandWithTwoParams(command:String, arg1: String, arg2: String):QueryMessage = {
    command match {
      case "adduser" =>  new AddUserMessage(arg1, arg2)
      case "removepermission" =>  new RemovePermissionMessage(arg1, arg2)
      case "login" => new LoginMessage(arg1, arg2)

      case _ => new InvalidQueryMessage
    }
  }

  /**
    * Parses commands with three parameters.
    *
    * @param command The command to check.
    * @param arg1 The fist parameter.
    * @param arg2 The second parameter.
    * @param arg3 The third parameter.
    * @return The QueryMessage message.
    *
    * @see AddPermissionMessage
    * @see InvalidQueryMessage
    */
  private def parseCommandWithThreeParams(command:String, arg1: String, arg2: String, arg3: String):QueryMessage = {
    command match {
      case "addpermission" => {
        arg3 match {
          case "read" =>  new AddPermissionMessage(arg1, arg2, EnumPermission.Read)
          case "readwrite" =>  new AddPermissionMessage(arg1, arg2, EnumPermission.ReadWrite)
          case _ =>  new InvalidQueryMessage
        }
      }

      case _ =>  new InvalidQueryMessage
    }
  }

  /**
    * Parses row-level commands with one parameter.
    *
    * @param command The command to check.
    * @param key The key of the row command.
    *
    * @see FindRowMessage
    * @see RemoveRowMessage
    * @see InvalidQueryMessage
    */
  private def parseRowCommandOneParam(command: String, key: String): QueryMessage = {
    command match {
      case "find" => new FindRowMessage(key)
      case "remove" =>  new RemoveRowMessage(key)

      case _ =>  new InvalidQueryMessage
    }
  }

  /**
    * Parses row-level commands with two parameters.
    *
    * @param command The command to check.
    * @param key The key of the row command.
    * @param value The value of the row command.
    *
    * @see InsertRowMessage
    * @see UpdateRowMessage
    * @see InvalidQueryMessage
    */
  private def parseRowCommandTwoParams(command: String, key: String, value: String): QueryMessage = {
    command match {
      case "insert" =>  new InsertRowMessage(key, value.getBytes("UTF-8"))
      case "update" =>  new UpdateRowMessage(key, value.getBytes("UTF-8"))

      case _ => new InvalidQueryMessage
    }
  }
}