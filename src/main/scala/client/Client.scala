package client
import java.io._
import java.net._
import java.util
import scala.util.matching.Regex

/**
  * Created by matteobortolazzo on 01/05/2016.
  */
object Client extends App {
  var socket: Socket = null
  var out: PrintStream = null
  var in: BufferedInputStream = null

  override def main(args: Array[String]): Unit = {
    Welcome.printWelcomeMessage

    // Readline loop
    for (ln <- io.Source.stdin.getLines) {
      executeLine(ln)
    }
  }


  /** Send the query to the server and wait for a response*/
  def sendQuery(query: String): Unit = {
    out.write(1) // 01 = query
    out.print(query)
    out.flush()
    while(in.available() < 1) Thread.sleep(100)
    val buf = new Array[Byte](in.available())
    in.read(buf)
    val input = new String(buf)
    println(input)
  }

  /**
    *
    */
  def executeLine(ln: String): Unit = {
    // If the client is connected
    if (socket != null && !socket.isClosed && socket.isConnected) {
      // Close the socket when the user write 'disconnect'
      if (ln == "disconnect") socket.close()
      else {
        // Send the query to the server
        sendQuery(ln)   ///////ADD checkInput method to ln here to automatically convert values to byte
      }
    }
    else {
      // Connection command pattern (connect address:port username password)
      val pattern = "connect\\s(.+):([0-9]*)\\s(.+)\\s(.+)".r
      val result = pattern.findFirstMatchIn(ln)
      // If it's a connection command
      if (result.isDefined) {
        val regex = result.get
        try {
          // Create the socket, the output stream and send the connection command to the server
          socket = new Socket(InetAddress.getByName(regex.group(1)), Integer.parseInt(regex.group(2)))
          out = new PrintStream(socket.getOutputStream)
          in = new BufferedInputStream(socket.getInputStream)
          sendQuery(ln)
        }
        catch {
          case e: IOException => println(e.getMessage)
          case e: SecurityException => println("Security error")
          case e: IllegalArgumentException => println("Invalid port number")
          case e: NullPointerException => println("The address is null")
          case _: Throwable => println("There was an error")
        }
      }
      else {
        println("Please connect first")
      }
    }
  }

  private def checkInput(ln: String): String = {
    val pattern = "(\\S+)\\s\\'(.+)\\'\\s(\\S+)$".r
    val result = pattern.findFirstMatchIn(ln)
    if (result.isDefined) {
      val m = result.get
      val aux = ln.replaceAll(m.group(3),util.Arrays.toString(m.group(3).getBytes()))
      println(aux.getBytes())
      return aux
    }
    else{
      return ln
    }
  }


}


//val in = new BufferedSource(socket.getInputStream)
//while (in.hasNext) {
//  print(in.next())
//}
//in.close()