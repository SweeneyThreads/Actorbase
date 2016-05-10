package client

import java.io.{OutputStream, PrintStream, BufferedInputStream}
import java.net.{InetAddress, Socket}

import client._
import org.scalatest.{Matchers, FlatSpec}
import org.scalamock.scalatest._
import org.scalamock.scalatest.MockFactory


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
