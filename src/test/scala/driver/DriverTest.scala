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

import java.io._
import java.net.{Socket, ServerSocket}
import java.nio.{ByteOrder, ByteBuffer}

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Paolo on 10/05/2016.
  */
class DriverTest  extends FlatSpec with Matchers with MockFactory{

  //FakeServer is a runnable that is used to simulate Actorbase server
  class FakeServer(val queryString: String= "a query") extends Runnable{
    //port number of the fake server
    val portNumber: Integer = 8080
    var stopped: Boolean = false
    var fakeSocket: ServerSocket = null
    var clientSocket: Socket = null
    var out: PrintStream = null
    var in: BufferedInputStream = null
    // value of the expected first byte. 1 for query
    val firstByte = 0

    try {
      // build a ServerSocket with the given portNumber
      fakeSocket = new ServerSocket(portNumber)
    }
    catch {
      case e: Exception =>
    }

    //overriding the run function of Runnable
    override def run(): Unit ={
      // Accepting a clientSocket
      clientSocket = fakeSocket.accept()
      // Initialize the output on socket
      out = new PrintStream(clientSocket.getOutputStream)
      // Initializing the input from socket
      in = new BufferedInputStream(clientSocket.getInputStream)
      while (!stopped) {
        // while there is nothing in the buffer sleep
        while (in.available < 8) Thread.sleep(100)
        // put the content of the buffer in the Array of Byte "buf"
        val buf = new Array[Byte](in.available())
        in.read(buf)
        // check if the first and last two bytes are correct
        if(buf(0) != 0 &&
          buf(1) != 1 &&
          buf(buf.length - 2) != 0 &&
          buf(buf.length - 1) != 2) throw newExpectationException("query start and end was incorrect")
        // remove two first byte
        val bufforlenght =buf.drop(2)
        // compute the length of the query
        val lengthBytes = new Array[Byte](4)
        bufforlenght.copyToArray(lengthBytes, 0, 4)
        val length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.LITTLE_ENDIAN).getInt()
        //create the string of the query
        val bufQuery = buf.slice(7, buf.length-2)
        val query: String = new String(bufQuery)
        println(query)
        if (query == "login admin admin") {
          out.print("Y")
          out.flush()
        }
        else if (query == "disconnect") {
          stopped = true
          out.print("Logout OK")
          out.flush()
        }
        // check if the query and the query length are correct
        else if (query == queryString) {
          out.print(length)
          out.flush()
        }
        else {
          out.print("N")
          out.flush()
        }
      }
      out.close()
      in.close()
      clientSocket.close()
      fakeSocket.close()
    }
  }
/*TU62 and TU63 and TU64*/
  "Driver connect method" should "create a new connection and return it with if a valid 'connect' command is given" in {
    val thread:Thread = new Thread(new FakeServer())
    thread.start()
    val conn:Connection = Driver.connect("localhost", 8080, "admin", "admin")
    conn.executeQuery("disconnect")
    conn match {
        //check if the connection is correct
      case c: ConcreteConnection => {
        c.host should be("localhost")
        c.port should be(8080)
        c.username should be("admin")
        c.password should be("admin")
      }
    }
  }
  /*TU62*/
  "Driver connect method" should "return null if an invalid 'connect' command is given" in {
    val thread:Thread = new Thread(new FakeServer())
    thread.start()
    val conn:Connection = Driver.connect("localht", 8080, "admin", "admin")
    //we must stop the server if we don't want to block the port so:
    val conn2 = new ConcreteConnection("localhost", 8080, "admin", "admin")
    conn2.executeQuery("disconnect")
    conn should be (null)
  }
    /*TU65*/
  "ConcreteConnection executeQuery method" should "send a correct string to server" in {
    val randomQuery:String = "Random query"
    val thread:Thread = new Thread(new FakeServer(randomQuery))
    thread.start()
    val conn:Connection = new ConcreteConnection("localhost", 8080, "admin", "admin")
    val response = conn.executeQuery(randomQuery)
    //controlling if connection send the correct query length
    response.toInt should be (randomQuery.length())
    conn.executeQuery("disconnect")
  }
}