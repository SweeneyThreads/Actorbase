/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 SWEeneyThreads
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 *
 * @author SWEeneyThreads
 * @version 0.0.1
 * @since 0.0.1
 */

package server.utils

import java.io.{FileNotFoundException, IOException}
import java.util

import org.json.JSONObject
import server.StaticSettings
import server.enums.EnumActorsProperties
import server.enums.EnumActorsProperties.ActorProperties

/**
  * Reads and writes various server configurations files.
  */
class ConfigurationManager() {

  /**
    * Reads from file doorkeepers' start port.
    * Whenever anyone call this method, it needs to catch the following Exceptions
    *
    * @throws IOException
    * @throws FileNotFoundException
    * @throws Exception
    * @param fileName The name of the file that contains the doorkeeper configuration.
    * @return A list with addresses and ports.
    */
  def readDoorkeepersSettings(fileName: String): util.ArrayList[Integer] = {
    val toReturn: util.ArrayList[Integer] = new util.ArrayList[Integer]()
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
  def readActorsProperties(fileName: String = "actor_properties.json"): Unit = {
    val source = scala.io.Source.fromFile("conf/"+fileName)
    val list = try source.getLines().mkString finally source.close()
    val jsonObject = new JSONObject(list)
    val properties = jsonObject.getJSONArray("properties")
    for (i <- 0 until properties.length()) {
      val singleProperty = properties.getJSONObject(i)
      val name = singleProperty.getString("name")
      name match {
        case "maxRowNumber" => StaticSettings.maxRowNumber = singleProperty.getInt("value")
        case "ninjaNumber" => StaticSettings.ninjaNumber = singleProperty.getInt("value")
        case "warehousemanNumber" => StaticSettings.warehousemanNumber = singleProperty.getInt("value")
        case "datapath" => StaticSettings.dataPath = singleProperty.getString("value")
      }
    }

  }
}