package client
import driver.{Connection, Driver}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/**
  * A command line client instance. Contains the main method of the command line client-side of Actorbase.
  */
object Client {

  private var connection: Connection = null

  def main(args: Array[String]): Unit = {
    Welcome.printWelcomeMessage
    // Readline loop
    print("> ")
    for (ln <- io.Source.stdin.getLines) {
      if(ln != "") {
        executeLine(ln.trim)
      }
      print("> ")
    }
  }

  /**
    * Processes a command line content executing the client-side query.
    * If the connection is not established checkLogin method is called.
    *
    * @param ln the query String to be processed
    */
  def executeLine(ln: String): Unit = {
    if(ln == "quit") {
      System.exit(1)
    }
    // Check if the client is connected
    if (connection != null) {
      // Close the connection if the the command is 'disconnect'
      if (ln == "disconnect") {
        connection.closeConnection()
        connection = null
        println("You are disconnected!")
      }
      // execute the query otherwise
      else {
        try {
          println(connection.executeQuery(ln))
        }catch{
          case ie:InterruptedException => connection.closeConnection(); connection = null; println("response time expired server, please reconnect to the server")
        }
      }
    }
    else checkLogin(ln)
  }

  /**
    * Tries to establish a connection in case of a login query, prints an error message otherwise.
    *
    * @param ln The query String to be processed
    */
  def checkLogin(ln:String): Unit = {
    // Connection command pattern (connect address:port username password)
    val pattern = "connect\\s(\\S+):([0-9]*)\\s(\\S+)\\s(\\S+)$".r
    val result = pattern.findFirstMatchIn(ln)
    // If it's a connection command
    if (result.isDefined) {
      val regex = result.get
      try {
        connection = Driver.connect(regex.group(1), Integer.parseInt(regex.group(2)), regex.group(3), regex.group(4))
      }catch{
        case ie:InterruptedException => connection.closeConnection(); connection = null; println("response time expired server, please reconnect to the server")
      }
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
      import java.utils.Base64
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