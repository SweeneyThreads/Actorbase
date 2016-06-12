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

package driver

import java.io.PrintStream
import java.net.{InetAddress, Socket}
import java.nio.{ByteBuffer, ByteOrder}

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

  private val socket = new Socket(InetAddress.getByName(host), port)
  private val out = new PrintStream(socket.getOutputStream)
  private val in = socket.getInputStream

  socket.setSoTimeout(10000)
  login(username,password)
  /**
    * Closes the connection, after invoking this method the connection cannot be used anymore.
    * Create a new object with the same parameters to re-establish the connection with the server.
    */
  def closeConnection(): Unit = {
    socket.close()
  }

  /**
    * Executes a query on the connected server.
    *
    * @param query The entire query to execute, expressed in Actorbase syntax as String
    * @return a the query result as a String, if the query requested was a value's request, the value is returned as a
    *         String
    */
  def executeQuery(query: String): String = {
    if(socket.isConnected) {
      out.write(0)
      out.write(1)
      out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(query.length).array())
      out.write(1) // 1 = query
      out.print(query)
      out.write(0)
      out.write(2)
      out.flush()
      while(in.available() < 1) Thread.sleep(100)
      val buf = new Array[Byte](in.available())
      try {
        in.read(buf)
      }catch{
        case ie:InterruptedException => throw new InterruptedException
      }
      new String(buf, "UTF-8")
    }
    else
      "This connection is no more valid"
  }

  /**
    * Logs in on the server if the TCP connection is established.
    *
    * @param username The username to use for the login as String
    * @param password The password to use for the login as String
    * @throws RuntimeException if the login procedure fails
    */
  private def login(username: String, password: String): Unit = {
    val login = "login " + username + " " + password
    val result = executeQuery(login)
    if(result != "Y") {
      socket.close()
      throw new RuntimeException
    }
  }
}
