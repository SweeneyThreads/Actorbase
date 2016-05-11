package driver

import java.io.{BufferedInputStream, PrintStream}
import java.net.{InetAddress, Socket}

/**
  * Created by eliamaino on 10/05/16.
  */
trait Connection {
  def closeConnection(): Unit

  def executeQuery(query: String): String

}

class ConcreteConnection(val host: String, val port: Integer, val username: String, val password: String) extends Connection {

  val socket = new Socket(InetAddress.getByName(host), port)
  val out = new PrintStream(socket.getOutputStream)
  val in = new BufferedInputStream(socket.getInputStream)

  login(username,password)

  def closeConnection(): Unit = {
    socket.close()
  }

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
