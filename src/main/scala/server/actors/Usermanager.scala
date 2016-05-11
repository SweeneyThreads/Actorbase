package server.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString
import server.Server
import server.messages.query.ErrorMessages.InvalidQueryMessage
import server.messages.query.{LoginMessage, QueryMessage}
import server.util.Parser

import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** Every client talks with an instance of this actor.
  * It creates messages from strings.
  * */
class Usermanager extends Actor with akka.actor.ActorLogging {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  import Tcp._

  val parser = new Parser()
  var connected = false
  var mainActor: ActorRef = null

  def receive = {
    case Received(data: ByteString) => {
      // The first byte represents the operation type (1 = query)
      val op = data.slice(0, 1).toArray
      op(0) match {
        case 1 => parseQuery(data.slice(1, data.length).utf8String)
        case _ => reply("Invalid command")
      }
    }
    case PeerClosed => {
      log.info("Client disconnected")
      context stop self
    }
  }

  private def parseQuery(query: String): Unit = {
    val message = parser.parseQuery(query)
    message match {
      case m:InvalidQueryMessage => reply("Invalid query")
      case m:QueryMessage => handleQueryMessage(m)
      case _ => log.error("Unhandled message in usermanager: " + message)
    }
  }

  /** Handles query messages */
  private def handleQueryMessage(message: QueryMessage): Unit = {
    message match {
      case LoginMessage(username, password) => handleLogin(username, password)
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

  /** Handles login messages */
  private def handleLogin(username: String, password: String): Unit = {
    if (!connected) {
      val psw = Server.users.get(username)
      if (psw != null && psw == password) {
        // If the login is valid it creates a main actor that contains the user permissions
        connected = true
        
        if(username == "admin") mainActor = context.actorOf(Props(new Main()))
        else mainActor = context.actorOf(Props(new Main(Server.permissions.get(username))))

        reply("Y")
        log.info(username + " is connected")
      }
      else reply("N")
    }
    else reply("N")
  }

  private def reply(str: String, sender: ActorRef = sender): Unit = {
    sender ! Write(ByteString(str))
  }
}
