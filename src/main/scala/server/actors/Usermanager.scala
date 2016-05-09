package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.util.{ByteString, Timeout}
import server.EnumPermission.Permission
import server.Server
import server.messages.ErrorMessages.InvalidQueryMessage
import server.messages.{ActorbaseMessage, ConnectMessage, ErrorMessages}
import server.util.Parser

import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** Every client talks with an instance of this actor.
  * It creates messages from strings.
  * */
class Usermanager extends Actor with akka.actor.ActorLogging {

  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  import Tcp._

  val parser = new Parser()
  var connected = false
  var mainActor: ActorRef = null

  def receive = {
    case Received(data: ByteString) => {
      // Takes the first byte
      val op = data.slice(0, 1).toArray
      if (op(0) == 1) {
        // Takes the query
        val query = data.slice(1, data.length).utf8String
        val message = parser.parseQuery(query)
        message match {
          case i: InvalidQueryMessage => reply("Invalid query")
          case _ => manageValidMessage(message)
        }
      }
    }
    case PeerClosed => {
      log.info("Client disconnected")
      context stop self
    }
  }

  /** Manage valid messages */
  private def manageValidMessage(message: ActorbaseMessage): Unit = {
    message match {
      case ConnectMessage(username, password) => manageLogin(username, password)
      case _ => {
        // If the user is connected it sends the message to the main actor
        if (connected) {
          val origSender = sender
          val future = mainActor ? message
          future.onComplete {
            case Success(result) => reply(result.toString, origSender)
            case Failure(t) => log.error("Error sending message: " + t.getMessage)
          }
        }
        // If someone sends a message before login
        else reply("WTF?! You can't be here if you're using our client, " +
          "so FUCK YOU and you're shitty home made actor-fuckin'-base client!")
      }
    }
  }

  /** Manage connection messages */
  private def manageLogin(username: String, password: String): Unit = {
    if (!connected) {
      val psw = Server.users.get(username)
      if (psw != null && psw == password) {
        // If the login is valid it creates a main actor that contains the user permissions
        connected = true
        
        if(username == "admin") mainActor = context.actorOf(Props(new Main()))
        else mainActor = context.actorOf(Props(new Main(Server.permissions.get(username))))

        reply("You're connected")
        log.info(username + " is connected")
      }
      else reply("Invalid login")
    }
    else reply("You're already connected")
  }

  private def reply(str: String, sender: ActorRef = sender): Unit = {
    sender ! Write(ByteString(str))
  }
}
