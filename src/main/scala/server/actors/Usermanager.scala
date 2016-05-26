package server.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString
import server.Server
import server.enums.{EnumPermission, EnumReplyResult}
import server.messages.query.ErrorMessages.InvalidQueryMessage
import server.messages.query.{LoginMessage, QueryMessage, ReplyMessage}
import server.utils.Parser

import scala.util.{Failure, Success}
import server.messages.query._
import server.messages.query.user.DatabaseMessages.CreateDatabaseMessage

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** Every client talks with an instance of this actor.
  * It creates messages from strings.
  * */
class Usermanager extends ReplyActor {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.language.postfixOps
  import scala.concurrent.duration._
  // Values for futures
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  import Tcp._
  // The parser instance
  val parser = new Parser()
  // Value that tells if
  var connected = false
  // The main actor reference
  var mainActor: ActorRef = null

  /** The main receive method */
  def receive = {
    // When it receive data
    case Received(data: ByteString) => {
      // The first byte represents the operation type
      val op = data.slice(0, 1).toArray
      // Match on the first byte
      op(0) match {
        // If  the byte is equals to 1 the command it's a query
        case 1 => parseQuery(data.slice(1, data.length).utf8String)
        case _ => reply("Invalid command")
      }
    }
    // When a client disconnects
    case PeerClosed => {
      log.info("Client disconnected")
      context stop self
    }
  }

  /** Parse the command like a query */
  private def parseQuery(query: String): Unit = {
    // Use the parser to parse the string
    val message = parser.parseQuery(query)
    message match {
      // If the user command is not a valid query
      case m:InvalidQueryMessage => reply("Invalid query")
      // If the user command is a valid query
      case m:QueryMessage => handleQueryMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString, currentMethodName()))
    }
  }

  /** Handles query messages */
  private def handleQueryMessage(message: QueryMessage): Unit = {
    message match {
      // If user types 'login <username> <password>'
      case LoginMessage(username, password) => handleLogin(username, password)
      case _ => {
        // If the user is connected it sends the message to the main actor
        if (connected) {
          // Save the original sender
          val origSender = sender
          // Send the message to the main and save the reply in a future
          val future = mainActor ? message
          future.onComplete {
            // If the main reply successfully
            case Success(result) => reply(replyBuilder.buildReply(result.asInstanceOf[ReplyMessage]), origSender)
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
    // if the user is connected
    if (!connected) {
      // Get the password
      val psw = Server.users.get(username)
      // If the user exists and it's equal to the one inserted
      if (psw != null && psw == password) {
        // If the login is valid it creates a main actor that contains the user permissions
        connected = true
        // 'admin' is the super admin so it has no permissions
        if(username == "admin") mainActor = context.actorOf(Props(new Main()))
        // If the user is not 'admin' the main receive the user's permissions
        else mainActor = context.actorOf(Props(new Main(Server.permissions.get(username))))
        reply("Y")
        log.info(username + " is connected")
      }
      else reply("N")
    }
    else reply("N")
  }

  /** Reply to the client */
  private def reply(str: String, sender: ActorRef = sender): Unit = {
    sender ! Write(ByteString(str))
  }
}
