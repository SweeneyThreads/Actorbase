package client
import java.io._
import java.net._

/**
  * Created by matteobortolazzo on 01/05/2016.
  */
object Client extends App {
  override def main(args: Array[String]): Unit = {

    var socket: Socket = null
    var out: PrintStream = null
    var in: BufferedInputStream = null
    Welcome.printWelcomeMessage

    // Readline loop
    for (ln <- io.Source.stdin.getLines) {
      // If the client is connected
      if (socket != null && socket.isConnected) {
        // Close the socket when the user write 'disconnect'
        if (ln == "disconnect") socket.close()
        // Send the query to the server
        else sendQuery(ln)
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
  }
}


//val in = new BufferedSource(socket.getInputStream)
//while (in.hasNext) {
//  print(in.next())
//}
//in.close()