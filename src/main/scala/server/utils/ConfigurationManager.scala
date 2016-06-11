package server.utils

import java.io.{FileNotFoundException, IOException}
import java.util
import org.json.JSONObject
import server.enums.EnumActorsProperties
import server.enums.EnumActorsProperties.ActorProperties

/*

EXAMPLES of JSON files nedeed by these methods
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

{
  "properties": [
    {
      "name" : "numberofninja",
      "value" : 4
    },
    {
      "name" : "numberofwarehouseman",
      "value" : 4
    },
    {
      "name" : "maxstorekeeper",
      "value" : 4
    },
    {
      "name" : "maxstorefinder",
      "value" : 4
    }
*/

/**
  * Created by matteobortolazzo on 31/05/2016.
  * Reads and writes various server configurations files.
  */
class ConfigurationManager() {

  /**
    * Reads from file doorkeepers' start address and port.
    * Whenever anyone call this method, it needs to catch the following Exceptions
    *
    * @throws IOException
    * @throws FileNotFoundException
    * @throws Exception
    * @param fileName The name of the file that contains the doorkeeper configuration.
    * @return A list with addresses and ports.
    */
  def readDoorkeepersSettings(fileName: String): util.ArrayList[Integer] = {
    val toReturn : util.ArrayList[Integer] = new util.ArrayList[Integer]()
    val source = scala.io.Source.fromFile(fileName)
    val list = try source.getLines().mkString finally source.close()
    val jsonObject = new JSONObject(list)
    val accesses = jsonObject.getJSONArray("ports")
    for (i <- 0 until accesses.length()) {
      val port = accesses.getInt(i)
      if (!toReturn.contains(port))
        toReturn.add(port)
    }
    return toReturn
  }

  /**
    * Reads from file actors' properties
    * Whenever anyone call this method, it needs to catch the following Exceptions
    *
    * @throws IOException
    * @throws FileNotFoundException
    * @throws Exception
    * @param fileName The name of the file that contains the actors properties.
    */
  def readActorsProperties(fileName: String = "actor_properties.json"): util.HashMap[ActorProperties, Integer] = {
    val toReturn : util.HashMap[ActorProperties, Integer] = new util.HashMap[ActorProperties, Integer]()

      val source = scala.io.Source.fromFile(fileName)
      val list = try source.getLines().mkString finally source.close()
      val jsonObject = new JSONObject(list)
      val properties = jsonObject.getJSONArray("properties")
      for( i <- 0 until properties.length()){
        val singleProperty = properties.getJSONObject(i)
        val name = singleProperty.getString("name")
        val prop = name match {
          case "numberofninja" => EnumActorsProperties.NumberOfNinjas
          case "numberofwarehouseman" => EnumActorsProperties.NumberOfWarehouseman
          case "maxstorefinder" => EnumActorsProperties.MaxStorefinderNumber
          case "maxstorekeeper" => EnumActorsProperties.MaxStorekeeperNumber
        }
        val num = singleProperty.getInt("value")
        toReturn.put(prop, num)
      }

    return toReturn
  }
}