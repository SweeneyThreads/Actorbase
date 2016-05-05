package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString
import server.EnumPermission.Permission
import server.Server
import server.messages.{ActorbaseMessage, ConnectMessage, InvalidQueryMessage}
import server.util.Parser

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** Every client talks with an instance of this actor.
  * It creates messages from strings.
  * */
class Usermanager extends Actor {
  import Tcp._

  val parser = new Parser()
  var connected = false
  var mainActor:ActorRef = null

  def receive = {
    case Received(data: ByteString) => {
      // Takes the first byte
      val op = data.slice(0, 1).toArray
      if(op(0) == 1) {
        // Takes the query
        val query  = data.slice(1, data.length).utf8String
        val message = parser.parseQuery(query)
        message match {
          case i: InvalidQueryMessage => println("Invalid query")
          case _ => manageValidMessage(message)
        }
      }
    }
    case PeerClosed => {
      println("Client disconnected")
      context stop self
    }
  }

  /** Manage valid messages */
  private def manageValidMessage(message: ActorbaseMessage): Unit = {
    message match {
      case ConnectMessage(username, password) => manageLogin(username, password)
      case _ => {
        // If the user is connected
        if (connected) mainActor ! message
        else println("WTF?! You can't be here if you're using our client, " +
          "so FUCK YOU and you're shitty home made actor-fuckin'-base client!")
      }
    }
  }

  /** Manage connection messages*/
  private def manageLogin(username:String, password:String): Unit ={
    if (!connected) {
      val psw = Server.users.get(username)
      if (psw != null && psw == password) {
        // If the login is valid it creates a main actor that contains the user permissions
        connected = true
        mainActor = context.actorOf(Props(new Main(Server.permissions.get(username))))
        println(username + " is connected")
      }
      else println("Invalid login")
    }
    else println("You're already connected")
  }
}

//sender() ! Write(data) // risponde al client
