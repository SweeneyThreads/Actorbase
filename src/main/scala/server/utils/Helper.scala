package server.utils

import java.util

import collection.JavaConversions._
import scala.collection.immutable.{HashMap, ListMap}

/**
  * Created by davidetommasin on 10/05/2016.
  */
class Helper {
  var helpMessages = new util.LinkedHashMap[String, String]
  helpMessages.put("help",      "       help <command>          show the command's description")
  helpMessages.put("listdb",    "       listdb                  show all available databases")
  helpMessages.put("selectdb",  "       selectdb <db_name>      select the database")
  helpMessages.put("createdb",  "       createdb <db_name>      create a database")
  helpMessages.put("deletedb",  "       deletedb <db_name>      delete the database")
  helpMessages.put("listmap",   "       listmap                 show all the maps within the selected database")
  helpMessages.put("selectmap", "       selectmap               select the map")
  helpMessages.put("createmap", "       createmap <map_name>    create a map")
  helpMessages.put("deletemap", "       deletemap <map_name>    delete the map")
  helpMessages.put("keys",      "       keys                    show all the keys within the selected map")
  helpMessages.put("find",      "       find '<key>'            show the value of the specified key")
  helpMessages.put("insert",    "       insert '<key>' <value>  insert an entry")
  helpMessages.put("update",    "       update '<key>' <value>  update the value of the specified key")
  helpMessages.put("remove",    "       remove '<key>'          remove the entry with the specified key")

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