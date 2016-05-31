package server.utils

import java.util

import akka.event.LoggingAdapter

/**
  * Created by borto on 31/05/2016.
  */
/*
{
  "accesses": [
    {
      "address": "localhost",
      "port": "8181"
    },
    {
      "address": "127.0.0.1",
      "port": "8282"
    }
  ]
}
*/

class ConfigurationReader(log:LoggingAdapter) {
  def readDoorkeepersSettings(fileName: String): util.HashMap[String, Integer] = {
    //TODO
    new util.HashMap[String, Integer]()
  }

  /*def readUsers(path: String): ConcurrentHashMap[String, String] = {
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
  }*/
}