package driver

import java.io.{BufferedInputStream, PrintStream}
import java.net.{InetAddress, Socket}

/**
  * Created by eliamaino on 10/05/16.
  */

/**
  * This trait defines a Connection interface.
  */
trait Connection {
  def closeConnection(): Unit

  def executeQuery(query: String): String

}

/**
  * This class defines a concrete connection with a server.
  *
  * @constructor
  * @param host The host name as String
  * @param port The host port as Integer
  * @param username The user username as String
  * @param password The user password as String
  */
class ConcreteConnection(val host: String, val port: Integer, val username: String, val password: String) extends Connection {

  val socket = new Socket(InetAddress.getByName(host), port)
  val out = new PrintStream(socket.getOutputStream)
  val in = new BufferedInputStream(socket.getInputStream)

  login(username,password)

  /**
    * Close the connection, after invoking this method the connection can't be used anymore. Create a new object with
    * the same parameters to re-establish the connection with the server.
    */
  def closeConnection(): Unit = {
    socket.close()
  }

  /**
    * Execute a query on the connected server.
    *
    * @param query The entire query to execute, expressed in Actorbase syntax as String
    * @return a the query result as a String, if the query requested was a value's request, the value is returned as a
    *         String
    */
  def executeQuery(query: String): String = {
    if(socket.isConnected) {
      out.write(1) // 01 = query
      out.print(query)
      out.flush()
      while(in.available() < 1) Thread.sleep(100)
      val buf = new Array[Byte](in.available())
      in.read(buf)
      new String(buf)
    }
    else
      "This connection is no more valid"
  }

  /**
    * Login on the server, after the TCP connection is established
    *
    * @param username The username to use for the login as String
    * @param password The password to use for the login as String
    * @throws RuntimeException if the login procedure fails
    */
  private def login(username: String, password: String): Unit = {
    val login = "login " + username + " " + password
    out.write(1)
    out.print(login)
    out.flush()
    while(in.available() < 1) Thread.sleep(100)
    val buf = new Array[Byte](in.available())
    in.read(buf)
    val result = new String(buf)
    if(result != "Y") {
      socket.close()
      throw new RuntimeException
    }
  }
}
