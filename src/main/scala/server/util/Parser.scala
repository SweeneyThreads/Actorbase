package server.util

import java.util

import akka.event.LoggingAdapter
import server.messages._
import server.messages.query.DatabaseMessages._
import server.messages.query.MapMessages._
import server.messages.query.RowMessages._
import ErrorMessages.InvalidQueryMessage
import server.messages.query.LoginMessage

import scala.util.matching.Regex

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
class Parser {

  def parseQuery(query: String, log:LoggingAdapter = null) : ActorbaseMessage = {
    // Connect command
    var pattern = "login\\s(\\S+)\\s(\\S+)$".r
    var m = getMatch(pattern, query)
    if(m != null) return new LoginMessage(m.group(1), m.group(2))

    // Row command (two parameters)
    pattern = "(\\S+)\\s\\'(.+)\\'\\s(\\S+)$".r
    m = getMatch(pattern, query)
    if(m != null) return parseRowCommandTwoParams(m.group(1), m.group(2), m.group(3))

    // Row command (one parameter)
    pattern = "(\\S+)\\s\\'(.+)\\'$".r
    m = getMatch(pattern, query)
    if(m != null) return parseRowCommandOneParam(m.group(1), m.group(2))

    // Command with parameters
    pattern = "(\\S+)\\s(\\S+)$".r
    m = getMatch(pattern, query)
    if(m != null) return parseCommandWithParam(m.group(1), m.group(2))

    // Command without parameters
    pattern = "(\\S+)$".r
    m = getMatch(pattern, query)
    if(m != null) return parseCommandWithoutParam(m.group(1))

    return new InvalidQueryMessage
  }

  /** Finds pattern matches on the command */
  private def getMatch(pattern:Regex, command:String): Regex.Match = {
    val result = pattern.findFirstMatchIn(command)
    if (result.isDefined) return result.get
    return null
  }

  /** Parses commands without parameters */
  private def parseCommandWithoutParam(command: String): ActorbaseMessage = {
    //renamed due to query without params for row level
    command match {
      case "listdb" => return new ListDatabaseMessage
      case "list" => return new ListMapMessage
      case "keys" => return new ListKeysMessage

      case _ => return new InvalidQueryMessage
    }
  }

  /** Parses commands with parameters */
  private def parseCommandWithParam(command: String, arg: String): ActorbaseMessage = {
    command match {
      case "selectdb" => return new SelectDatabaseMessage(arg)
      case "createdb" => return new CreateDatabaseMessage(arg)
      case "deletedb" => return new DeleteDatabaseMessage(arg)

      case "select" => return new SelectMapMessage(arg)
      case "create" => return new CreateMapMessage(arg)
      case "delete" => return new DeleteMapMessage(arg)

      case _ => return new InvalidQueryMessage
    }
  }

  /** Parses row level commands with one parameter */
  private def parseRowCommandOneParam(command: String, key: String): ActorbaseMessage = {
    command match {
      case "find" => return new FindRowMessage(key)
      case "remove" => return new RemoveRowMessage(key)

      case _ => return new InvalidQueryMessage
    }
  }

  /** Parses row level commands with two parameters */
  private def parseRowCommandTwoParams(command: String, key: String, value: String): ActorbaseMessage = {
    command match {
      case "insert" => return new InsertRowMessage(key, value)
      case "update" => return new UpdateRowMessage(key, value)

      case _ => return new InvalidQueryMessage
    }
  }
}
