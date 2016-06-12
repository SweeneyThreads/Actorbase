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

import java.net.Socket

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}


/**
  * Created by kurt on 08/05/2016.
  */
class ClientTest extends FlatSpec with Matchers with MockFactory{
  //testing Base64 conversion of 2-args commands
  //////////////////commented until client wont have conversion////////////////////////////
  /*"Only 2-param commands" should "have the 2nd param converted in byte via base64 conversion" in {
    //testing random command with 2 param
    Client.convertQuery("RANDOMCOMMAND 'randomKey' randomValue") should be ("RANDOMCOMMAND 'randomKey' cmFuZG9tVmFsdWU=")
    //testing insert command
    Client.convertQuery("insert 'foo' bar") should be ("insert 'foo' YmFy")
    //testing update command
    Client.convertQuery("update 'rum' Pirates") should be ("update 'rum' UGlyYXRlcw==")
    //testing commands with 0 and 1 param not to be converted
    Client.convertQuery("find 'rum'") should be ("find 'rum'")
    Client.convertQuery("list") should be ("list")
  }*/

  /**
    * @todo test on executeLine method.
    */

  val openSocketStub = stub[Socket]
  (openSocketStub.isClosed _).when().returns(false)
  (openSocketStub.isConnected _).when().returns(true)

  "Client window (connected)" should "disconnect when 'disconnect' command is called" in {
    //Client.socket=openSocketStub
    Client.executeLine("disconnect")
    //(Client.socket.close _).verify()
  }


  /*
  it should "send the query to the server if the command is not the 'disconnect' command" in {
    val mockStream : PrintStream = new PrintStream(new OutputStream {
      override def write(b: Int): Unit = {}
    })
    Client.socket=openSocketStub
    mockStream.write(1)
    mockStream.print("listdb")
    mockStream.flush()
    Client.executeLine("listdb")
    Client.out should be (mockStream)
  }*/


}
