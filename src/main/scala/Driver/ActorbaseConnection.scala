package Driver

import java.io.{BufferedInputStream, PrintStream}
import java.net.{InetAddress, Socket}

/**
  * Created by eliamaino on 10/05/16.
  */
class ActorbaseConnection(host: String, port: Integer, username: String, password: String) {

  val socket = new Socket(InetAddress.getByName(host), port)
  val out = new PrintStream(socket.getOutputStream)
  val in = new BufferedInputStream(socket.getInputStream)

  println(executeQuery("login " + username + " " + password))

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
}
