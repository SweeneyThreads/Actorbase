package server.util

import server.messages._

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
object Parser {

  def parseQuery(query: String) : ActorbaseMessage = {
    //Row command (two param)
    var pattern = "(.+)\\s\\'(.+)\\'\\s(.+)".r
    var result = pattern.findFirstMatchIn(query)
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
    //System command without param
    pattern = "(.+)".r
    result = pattern.findFirstMatchIn(query)
    if(result.isDefined) {
      val regex = result.get
      return parseSystemCommandWithoutParam(regex.group(1))
    }

    return new InvalidQueryMessage
  }

  private def parseSystemCommandWithoutParam(command: String): ActorbaseMessage = {
    command match {
      case "listdb" => return new ListDatabaseMessage
      case "list" => return new ListMapMessage

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
