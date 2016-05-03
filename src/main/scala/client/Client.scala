package client
import java.io._
import java.net._
import java.nio.ByteBuffer

import scala.io.BufferedSource

/**
  * Created by matteobortolazzo on 01/05/2016.
  */
object Client extends App {

  override def main(args: Array[String]): Unit = {
    val socket = new Socket(InetAddress.getByName("localhost"), 8181)
    for (ln <- io.Source.stdin.getLines) {
      if (ln == "close")
        socket.close()
      val out = new PrintStream(socket.getOutputStream())
      out.write(1) // 1 = query
      out.print(ln)
      out.flush()
    }
    //val in = new BufferedSource(socket.getInputStream())
    //while (in.hasNext) {
    //  print(in.next())
    //}
    //in.close()
  }
}