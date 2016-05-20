package driver

import java.io._
import java.net.{Socket, ServerSocket}

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Paolo on 10/05/2016.
  */
class DriverTest  extends FlatSpec with Matchers with MockFactory{

  //FakeServerLogin is a runnable that is used to simulate Actorbase server for the entire login process
  class FakeServerLogin extends Runnable{
    //port number of the fake server
    val portNumber: Integer = 8080

    //overriding the run function of Runnable
    override def run(): Unit ={
      try {
        // build a ServerSocket with the given portNumber
        val fakeSocket: ServerSocket = new ServerSocket(portNumber)
        // Accepting a clientSocket
        val clientSocket = fakeSocket.accept()
        // Initialize the output on socket
        val out = new PrintStream(clientSocket.getOutputStream)
        // Initializing the input from socket
        val in = new BufferedInputStream(clientSocket.getInputStream)
        // while there is nothing in the buffer sleep
        while (in.available < 1) Thread.sleep(100)
        // put the content of the buffer in the Array of Byte "buf"
        val buf = new Array[Byte](in.available())
        in.read(buf)
        // value of the expected first byte. 1 for query
        val firstByte: Byte = 1
        // creating a byte array without the first byte and cast it to String
        val bufQuery = buf.slice(1, buf.length)
        val query: String = new String(bufQuery)
        // chec if the first byte and the query are correct
        if (buf(0) == firstByte && query == "login admin admin") {
          out.print("Y")
          out.flush()
          //closing socket
          in.close()
          out.close()
          fakeSocket.close()
        }
        else {
          out.print("N")
          out.flush()
          //closing socket
          in.close()
          out.close()
          fakeSocket.close()
        }
      }
      catch {
        case e: Exception =>
      }
    }
  }

  //FakeServerLogin is a runnable that is used to simulate Actorbase server for the entire login process
  class FakeServer2 extends Runnable{
    //port number of the fake server
    val portNumber: Integer = 8080

    //overriding the run function of Runnable
    override def run(): Unit ={
      try {
        // build a ServerSocket with the given portNumber
        val fakeSocket: ServerSocket = new ServerSocket(portNumber)
        // Accepting a clientSocket
        val clientSocket = fakeSocket.accept()
        // Initialize the output on socket
        val out = new PrintStream(clientSocket.getOutputStream)
        // Initializing the input from socket
        val in = new BufferedInputStream(clientSocket.getInputStream)
        // while there is nothing in the buffer sleep
        while (in.available < 1) Thread.sleep(100)
        // put the content of the buffer in the Array of Byte "buf"
        var buf = new Array[Byte](in.available())
        in.read(buf)
        // value of the expected first byte. 1 for query
        val firstByte: Byte = 1
        // creating a byte array without the first byte and cast it to String
        var bufQuery = buf.slice(1, buf.length)
        var query: String = new String(bufQuery)
        // chec if the first byte and the query are correct
        if (buf(0) == firstByte && query == "login admin admin") {
          out.print("Y")
          out.flush()
        }
        else {
          out.print("N")
          out.flush()
        }
        while (in.available < 1) Thread.sleep(100)
        // put the content of the buffer in the Array of Byte "buf"
        buf = new Array[Byte](in.available())
        in.read(buf)
        // creating a byte array without the first byte and cast it to String
        bufQuery = buf.slice(1, buf.length)
        query = new String(bufQuery)
        // chec if the first byte and the query are correct
        if (buf(0) == firstByte && query == "random query") {
          out.print("Y")
          out.flush()
          //closing socket
          in.close()
          out.close()
          fakeSocket.close()
        }
        else {
          out.print("N")
          out.flush()
          //closing socket
          in.close()
          out.close()
          fakeSocket.close()
        }
      }
      catch {
        case e: Exception =>
      }
    }
  }

  "Driver connect method" should "create a new connection and return it with if a valid 'connect' command is given" in {
    val thread:Thread = new Thread(new FakeServerLogin())
    thread.start()
    val conn:Connection = Driver.connect("localhost", 8080, "admin", "admin")
    conn match {
      case c: ConcreteConnection => {
        c.host should be("localhost")
        c.port should be(8080)
        c.username should be("admin")
        c.password should be("admin")
      }
    }
  }

  "Driver connect method" should "return null if an invalid 'connect' command is given" in {
    val thread:Thread = new Thread(new FakeServerLogin())
    thread.start()
    val conn:Connection = Driver.connect("localhost", 8080, "ad", "admin")
    conn should be (null)
  }

  "ConcreteConnection executeQuery method" should "send a correct string to server" in {
    val thread:Thread = new Thread(new FakeServer2())
    thread.start()
    val conn:Connection = new ConcreteConnection("localhost", 8080, "admin", "admin")
    val response = conn.executeQuery("random query")
    response should be ("Y")
  }
}