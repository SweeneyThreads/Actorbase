package server.utils

import java.util

import collection.JavaConversions._
import scala.collection.immutable.{HashMap, ListMap}

/**
  * Created by davidetommasin on 10/05/2016.
  */
class Helper {
  var helpMessages = new util.LinkedHashMap[String, String]
  helpMessages.put("listdb",    "LISTDB                lists all available databases")
  helpMessages.put("selectdb",  "SELECTDB  nameDB      select the database")
  helpMessages.put("createdb",  "CREATEDB  nameDB      create the database with the name specied")
  helpMessages.put("deletedb",  "DELETEDB  nameDB      clears the databse with the name specied")
  helpMessages.put("listmap",   "LISTMAP               lists all maps within the database")
  helpMessages.put("selectmap", "SELECTMAP map         select the map to use")
  helpMessages.put("createmap", "CREATEMAP map         creates the map with the name specied")
  helpMessages.put("deletemap", "DELETEMAP map         deletes the map the name specied")
  helpMessages.put("keys",      "KEYS                  lists all the keys in the map")
  helpMessages.put("find",      "FIND      key         returns the value associated with the key searched")
  helpMessages.put("remove",    "REMOVE    key         deletes the key and its value")
  helpMessages.put("insert",    "INSERT    key value   insertion of a key and its value in a previously selected database")
  helpMessages.put("update",    "UPDATE    key value   update of a key and its value in a previously selected database")

  def completeHelp(): String = {
    var helpComplete = ""
    for (k: String <- helpMessages.keySet()) helpComplete += helpMessages.get(k) + "\n"
    return helpComplete.substring(0, helpComplete.length - 1)
  }

  def specificHelp(command: String): String = {
    val help = helpMessages.get(command.toLowerCase())
    if (help != null)
      return help
    return command + " is not a valid command"
  }
}