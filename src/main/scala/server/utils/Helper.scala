package server.utils

import java.util

import scala.collection.JavaConversions._

/**
  * Created by matteobortolazzo on 10/05/2016.
  * Gives commands descriptions.
  */
class Helper {
  val helpMessages = new util.LinkedHashMap[String, String]
  helpMessages.put("help",            "       help <command>                                              show the command's description")
  helpMessages.put("listdb",          "       listdb                                                      show all available databases")
  helpMessages.put("selectdb",        "       selectdb <database_name>                                    select the database")
  helpMessages.put("createdb",        "       createdb <database_name>                                    create a database")
  helpMessages.put("deletedb",        "       deletedb <database_name>                                    delete the database")
  helpMessages.put("listmap",         "       listmap                                                     show all the maps within the selected database")
  helpMessages.put("selectmap",       "       selectmap <map_name>                                        select the map")
  helpMessages.put("createmap",       "       createmap <map_name>                                        create a map")
  helpMessages.put("deletemap",       "       deletemap <map_name>                                        delete the map")
  helpMessages.put("keys",            "       keys                                                        show all the keys within the selected map")
  helpMessages.put("find",            "       find '<key>'                                                show the value of the specified key")
  helpMessages.put("insert",          "       insert '<key>' <value>                                      insert an entry")
  helpMessages.put("update",          "       update '<key>' <value>                                      update the value of the specified key")
  helpMessages.put("remove",          "       remove '<key>'                                              remove the entry with the specified key")
  helpMessages.put("listuser",        "       listuser                                                    show the list of all the users")
  helpMessages.put("adduser",         "       adduser <username> <password>                               add a new user")
  helpMessages.put("removeuser",      "       removeuser <username>                                       remove an existing user")
  helpMessages.put("listpermissions", "       listpermissions <username>                                  show the list of the permissions an user has")
  helpMessages.put("addpermission",   "       addpermission <username> <database_name> <permission_type>  add or update an user permission for a specific database")
  helpMessages.put("removepermission","       removepermission <username> <database_name>                 remove an existing user permission")

  val SpecificHelpMessages = new util.LinkedHashMap[String, String]
  SpecificHelpMessages.put("help",
                              "       help <command>\n" +
                              "         • command: the command how to know more information\n" +
                              "       Show the command's description.")
  SpecificHelpMessages.put("listdb",
                              "       listdb\n" +
                              "       List all the databases you have at least read permissions on.")
  SpecificHelpMessages.put("selectdb",
                              "       selectdb <database_name>\n" +
                              "         • database_name: the name to of the database to select.\n" +
                              "       This command is used to select a database.\n" +
                              "       If the database is selected the client shows a success message.\n" +
                              "       If a database with that name doesn't exist or you have insufficient permissions, it shows an error message.")
  SpecificHelpMessages.put("createdb",
                              "       createdb <database_name>\n" +
                              "         • database_name: the name to of the new database.\n" +
                              "       This command is used to create a new database.\n"+
                              "       If the database is created the client shows a success message.\n" +
                              "       If a database with that name already exists it shows an error message.")
  SpecificHelpMessages.put("deletedb",
                              "       deletedb <database_name>\n" +
                              "         • database_name: the name to of the database to delete.\n" +
                              "       This command is used to delete an existing database database.\n" +
                              "       If the database is deleted the client shows a success message.\n" +
                              "       If a database with that name doesn't exist it shows an error message.")
  SpecificHelpMessages.put("listmap",
                              "       listmap\n" +
                              "       List all the maps in the selected database.")
  SpecificHelpMessages.put("selectmap",
                              "       selectmap <map_name>\n" +
                              "         • map_name: the name to of the map to select.\n" +
                              "       This command is used to select a map.\n" +
                              "       If the map is selected the client shows a success message.\n" +
                              "       If a map with that name doesn't exist or you have insufficient permissions, it shows an error message.")
  SpecificHelpMessages.put("createmap",
                              "       createmap <map_name>\n" +
                              "         • map_name: the name to of the new map.\n" +
                              "       This command is used to create a map.\n" +
                              "       If the map is created the client shows a success message.\n" +
                              "       If a map with that name already exists it shows an error message.")
  SpecificHelpMessages.put("deletemap",
                              "       deletemap <map_name>\n" +
                              "         • map_name: the name to of the map to delete.\n" +
                              "       This command is used to delete the map.\n" +
                              "       If the map is deleted the client shows a success message.\n" +
                              "       If a map with that name doesn't exist it shows an error message.")
  SpecificHelpMessages.put("keys",
                              "       keys\n" +
                              "       This command is used to get the list of all the keys in the selected map.")
  SpecificHelpMessages.put("find",
                              "       find '<key>'\n" +
                              "         • key: the key of the row.\n" +
                              "       This command is used to get the value of a key.\n" +
                              "       If a row with that key exist the client shows the value, otherwise it shows an error message.")
  SpecificHelpMessages.put("insert",
                              "       insert '<key>' <value>\n" +
                              "         • key: the key of the row.\n" +
                              "         • value: the value of the row.\n" +
                              "       This command is used to insert a new row.\n" +
                              "       If the row is inserted the client shows a success message.\n" +
                              "       If a row with that key already exists it shows an error message.")
  SpecificHelpMessages.put("update",
                              "       update '<key>' <value>\n" +
                              "         • key: the key of the row.\n" +
                              "         • value: the value of the row.\n" +
                              "       This command is used to update the value of a row.\n" +
                              "       If the row is updated the client shows a success message.\n" +
                              "       If a row with that key doesn't exists it shows an error message.")
  SpecificHelpMessages.put("remove",
                              "       remove '<key>'\n" +
                              "         • key: the key of the row.\n" +
                              "       This command is used to remove an existing row.\n" +
                              "       If the entry is deleted the client shows a success message.\n" +
                              "       If an entry with that key doesn't exist it shows an error message.")
  SpecificHelpMessages.put("listuser",
                              "       listuser\n" +
                              "       This command is used to get the list of all the users.")
  SpecificHelpMessages.put("adduser",
                              "       adduser <username> <password>\n" +
                              "         • username: the username used to log in.\n" +
                              "         • password: the password used to log in.\n" +
                              "       This command is used to add a new user.\n" +
                              "       If the user is added the client shows a success message.\n" +
                              "       If a user with that username already exists it shows an error message.")
  SpecificHelpMessages.put("removeuser",
                              "       removeuser <username>\n" +
                              "         • username: the username to remove.\n" +
                              "       This command is used to remove an existing user.\n" +
                              "       If the user is removed the client shows a success message.\n" +
                              "       If a user with that username doesn't exist it shows an error message.\n" +
                              "       NOTICE: if a user is removed all its permissions are removed.")
  SpecificHelpMessages.put("listpermissions",
                              "       listpermissions <username>\n" +
                              "         • username: the user username.\n" +
                              "       This command is used to get the list of the permissions an user has.")
  SpecificHelpMessages.put("addpermission",
                              "       addpermission <username> <database_name> <permission_type>\n" +
                              "         • username: the user username.\n" +
                              "         • database_name: the database name.\n" +
                              "         • permission_type: the permission type, it can be read or readwrite.\n" +
                              "       This command is used to add or update an user permission for a specific database.\n" +
                              "       If the user permission is added the client shows a success message.\n" +
                              "       If an user with that username or a database with that name doesn't exists it shows ad error.")
  SpecificHelpMessages.put("removepermission",
                              "       removepermission <username> <database_name>\n" +
                              "         • username: the user username.\n" +
                              "         • database_name: the database name.\n" +
                              "       This command is used to remove an existing user permission.\n" +
                              "       If the user permission is removed the client shows a success message.\n" +
                              "       If permission for that database of that username doesn't exists it shows an error.")

  /**
    * Returns the complete list of server's commands.
    *
    * @return The list of server commands.
    */
  def completeHelp(): String = {
    var helpComplete = ""
    for (k: String <- helpMessages.keySet()) helpComplete += helpMessages.get(k) + "\n"
    return helpComplete.substring(0, helpComplete.length - 1)
  }

  /**
    * Returns the description of the specific command.
    *
    * @param command The command
    * @return The description of the command
    */
  def specificHelp(command: String): String = {
    val help = SpecificHelpMessages.get(command.toLowerCase())
    if (help != null)
      return help
    return command + " is not a valid command"
  }
}