package server.util

import server.messages._

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
class Parser {

  def parseQuery(query: String) : ActorbaseMessage = {
    //Connect message
    var pattern = "connect\\s(.+):([0-9]*)\\s(.+)\\s(.+)".r
    var result = pattern.findFirstMatchIn(query)
    if (result.isDefined) {
      val regex = result.get
      return new ConnectMessage(regex.group(3), regex.group(4))
    }
    //Row command (two param)
    pattern = "(.+)\\s\\'(.+)\\'\\s(.+)".r
    result = pattern.findFirstMatchIn(query)
    if(result.isDefined) {
      val regex = result.get
      return parseRowCommandTwoParams(regex.group(1), regex.group(2), regex.group(3))
    }
    //Row command (one param)
    pattern = "(.+)\\s\\'(.+)\\'".r
    result = pattern.findFirstMatchIn(query)
    if(result.isDefined) {
      val regex = result.get
      return parseRowCommandOneParam(regex.group(1), regex.group(2))
    }
    //System command with param
    pattern = "(.+)\\s(.+)".r
    result = pattern.findFirstMatchIn(query)
    if(result.isDefined) {
      val regex = result.get
      return parseSystemCommandWithParam(regex.group(1), regex.group(2))
    }
    //System command without param OR Row command (no param)
    pattern = "(.+)".r
    result = pattern.findFirstMatchIn(query)
    if(result.isDefined) {
      val regex = result.get
      return parseCommandWithoutParam(regex.group(1))
    }

    return new InvalidQueryMessage
  }

  private def parseCommandWithoutParam(command: String): ActorbaseMessage = {
    //renamed due to query without params for row level
    command match {
      case "listdb" => return new ListDatabaseMessage
      case "list" => return new ListMapMessage
      case "keys" => return new ListKeysMessage

      case _ => return new InvalidQueryMessage
    }
  }

  private def parseSystemCommandWithParam(command: String, arg: String): ActorbaseMessage = {
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

  private def parseRowCommandOneParam(command: String, key: String): ActorbaseMessage = {
    command match {
      case "find" => return new FindRowMessage(key)
      case "remove" => return new RemoveRowMessage(key)

      case _ => return new InvalidQueryMessage
    }
  }

  private def parseRowCommandTwoParams(command: String, key: String, value: String): ActorbaseMessage = {
    command match {
      case "insert" => return new InsertRowMessage(key, value.getBytes())
      case "update" => return new UpdateRowMessage(key, value.getBytes())

      case _ => return new InvalidQueryMessage
    }
  }
}
