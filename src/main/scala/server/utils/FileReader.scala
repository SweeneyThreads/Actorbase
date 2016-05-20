package server.utils

import java.io.{FileNotFoundException, IOException}
import java.util.concurrent.ConcurrentHashMap

import akka.event.LoggingAdapter
import org.json.JSONObject
import server.enums.EnumPermission
import server.enums.EnumPermission.UserPermission

/**
  * Created by matteobortolazzo on 09/05/2016.
  */

/*
{
  "accounts": [
    {
      "id": "admin",
      "pw": "admin"
    },
    {
      "id": "borto",
      "pw": "borto"
    }
  ]
}
*/
/*
{
  "permissions": [
    {
      "id": "admin",
      "list": [
        {
          "name": "test",
          "perm": 1
        },
        {
          "name": "test2",
          "perm": 1
        }
      ]
    },
    {
      "id": "borto",
      "list": [
        {
          "name": "test",
          "perm": 1
        },
        {
          "name": "test2",
          "perm": 0
        }
      ]
    }
  ]
}
*/
class FileReader(log:LoggingAdapter) {

  def readUsers(path: String): ConcurrentHashMap[String, String] = {
    val users = new ConcurrentHashMap[String, String]()

    try {
      //* Open the file that should be on the same level as SRC folder */
      val source = scala.io.Source.fromFile(path)
      //* Loads the list of user from the file and close the file */
      val list = try source.getLines().mkString finally source.close()
      val jsonObject = new JSONObject(list)
      val accounts = jsonObject.getJSONArray("accounts")
      for (i <- 0 until accounts.length()) {
        val singleAccount = accounts.getJSONObject(i)
        val id = singleAccount.getString("id")
        val pw = singleAccount.getString("pw")
        users.put(id, pw)
      }
    }
    catch {
      case e: FileNotFoundException => log.warning("File " + path + " not found")
      case e: IOException => log.warning("Error while reading " + path)
      case e: Exception => log.warning(e.getMessage)
    }

    return users;
  }

  def readPermissions(path:String): ConcurrentHashMap[String, ConcurrentHashMap[String, UserPermission]] = {
    val permissions = new ConcurrentHashMap[String, ConcurrentHashMap[String, UserPermission]]()

    try {
      //* Open the file that should be on the same level as SRC folder */
      val source = scala.io.Source.fromFile(path)
      //* Loads the list of user from the file and close the file */
      val list = try source.getLines().mkString finally source.close()
      val jsonObject = new JSONObject(list)
      val permissionsList = jsonObject.getJSONArray("permissions")
      for (i <- 0 until permissionsList.length()) {
        val singleUserEntry = permissionsList.getJSONObject(i)
        val accountID = singleUserEntry.getString("id")
        val permissionList = singleUserEntry.getJSONArray("list")
        val permissionsMap = new ConcurrentHashMap[String, UserPermission]()
        for (j <- 0 until permissionList.length()) {
          val singleDbPerm = permissionList.getJSONObject(j)
          val DBName = singleDbPerm.getString("name")
          val permOnDB = singleDbPerm.getInt("perm")
          if (permOnDB == 0)
            permissionsMap.put(DBName, EnumPermission.Read)
          else
            permissionsMap.put(DBName, EnumPermission.ReadWrite)
          permissions.put(accountID, permissionsMap)
        }
      }
    }
    catch {
      case e: FileNotFoundException => log.warning("File " + path + " not found")
      case e: IOException => log.warning("Error while reading " + path)
      case e:Exception => log.warning(e.getMessage)
    }

    return permissions
  }
}
