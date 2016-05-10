package client

import java.io.{OutputStream, PrintStream, BufferedInputStream}
import java.net.{InetAddress, Socket}

import Driver.{ActorbaseConnectionProxy, Connection}
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

  //creating a class that extends Connection trait (the one that expose the driver interface)
  //FakeActorbaseConnection have empty methods
  class FakeActorbaseConnection extends Connection{
    def closeConnection(): Unit = {}
    def executeQuery(query: String): String = ""
  }

  "Client window (connected)" should "disconnect when 'disconnect' command is called" in {
    Client.connection=mock[FakeActorbaseConnection]
    Client.connection.closeConnection _ expects ()
    Client.executeLine("disconnect")
    Client.connection should be(null)
  }

  /**
    * @todo strange "null" println, fix it in the future
    */
  it should "send the query to the driver if the command is not 'disconnect'" in {
    Client.connection=mock[FakeActorbaseConnection]
    val someCommands=Array("listdb","list","insert 'key' value","keys","find 'key'")
    for (command <- someCommands) {
      Client.connection.executeQuery _ expects command
      Client.executeLine(command)
    }
  }

  "Client window (disconnected)" should "set this.connection with a proper ActorbaseConnection when 'connect' command is called properly" in {
    Client.connection=null
    Client.executeLine("connect localhost:8181 admin admin")
    Client.connection match {
      case a: ActorbaseConnectionProxy => {
        a.host should be ("localhost")
        a.port should be (8181)
        a.username should be ("admin")
        a.password should be ("admin")
      }
    }
  }

  it should "not create any connection if the command differs from a well formed 'connect' command" in {
    Client.connection=null
    val someCommands=Array("a random line with no sense at all","connect localhost admin admin","connect 8181 usr psw","connect localhost:8181 usr psw anotherParam","connect localhost:8181 usr")
    for (command <- someCommands) {
      Client.executeLine(command)
      if (Client.connection != null) {
        Client.connection match {
          case a: ActorbaseConnectionProxy => {
            println("HERE'S THE ONE THAT FAILS:")
            println("host= "+a.host)
            println("port= "+a.port)
            println("user= "+a.username)
            println("psw= "+a.password)
            println("-------------------------------")
          }
        }
      }
      Client.connection should be(null)
    }
  }
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

