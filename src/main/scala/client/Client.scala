package client
import java.io._

import driver.{Connection, Driver}

import scala.util.matching.Regex

/**
  * Created by matteobortolazzo on 01/05/2016.
  */
object Client extends App {

  var connection: Connection = null

  override def main(args: Array[String]): Unit = {
    Welcome.printWelcomeMessage
    // Readline loop
    for (ln <- io.Source.stdin.getLines) {
      executeLine(ln)
    }
  }

  def executeLine(ln: String): Unit = {
    if(ln == "quit") {
      System.exit(1)
    }
    // If the client is connected
    if (connection != null) {
      // Close the connection when the user write 'disconnect'
      if (ln == "disconnect") {
        connection.closeConnection()
        connection = null
        println("You are disconnected!")
      }
      else {
        println(connection.executeQuery(ln))
      }
    }
    else checkLogin(ln)
  }

  def checkLogin(ln:String): Unit = {
    // Connection command pattern (connect address:port username password)
    val pattern = "connect\\s(\\S+):([0-9]*)\\s(\\S+)\\s(\\S+)$".r
    val result = pattern.findFirstMatchIn(ln)
    // If it's a connection command
    if (result.isDefined) {
      val regex = result.get
      connection = Driver.connect(regex.group(1), Integer.parseInt(regex.group(2)), regex.group(3), regex.group(4))
      if (connection != null) {
        println("You are connected!")
      }
      else {
        println("Connection failed")
      }
    }
    else {
      println("Please connect first")
    }
  }


  /*def convertQuery(query:String): String = {
      var command = query
      import java.util.Base64
      import java.nio.charset.StandardCharsets

      val keyvalue = getKeyValue(query)
      // If the command is an insert or an udpdate, change the command to send to the server
      if(keyvalue != null)
        // Convert the value to Base64
        command = keyvalue._1 + " '" + keyvalue._2 + "' " + Base64.getEncoder.encodeToString(keyvalue._3.getBytes(StandardCharsets.UTF_8))
      return command
    }

    /** Returns the key and the value if it's an insert or update command*/
    def getKeyValue(query:String): (String, String, String) = {
      val pattern = "(\\S+)\\s\\'(.+)\\'\\s(\\S+)$".r
      val result = pattern.findFirstMatchIn(query)
      if (result.isDefined)
        return (result.get.group(1), result.get.group(2), result.get.group(3))
        return null
    }*/
}