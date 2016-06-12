
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

package client
import driver.{Connection, Driver}


/**
  * A command line client instance. Contains the main method of the command line client-side of Actorbase.
  */
object Client {

  private var connection: Connection = null

  def main(args: Array[String]): Unit = {
    Welcome.printWelcomeMessage()
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
}