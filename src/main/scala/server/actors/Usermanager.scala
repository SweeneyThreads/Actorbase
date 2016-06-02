package server.actors

import java.nio.{ByteBuffer, ByteOrder}
import java.util

import akka.actor.{ActorRef, Props}
import akka.dispatch.ExecutionContexts._
import akka.io.Tcp
import akka.pattern.ask
import akka.util.{ByteString, ByteStringBuilder, Timeout}
import server.enums.EnumPermission.UserPermission
import server.{Server, StoremanagersRefs}
import server.enums.{EnumPermission, EnumReplyResult}
import server.messages.query.ErrorMessages.InvalidQueryMessage
import server.messages.query.user.RowMessages.{FindInfo, FindRowMessage, KeyAlreadyExistInfo, StorefinderRowMessage}
import server.messages.query.{LoginMessage, QueryMessage, ReplyMessage, ServiceErrorInfo}
import server.utils.{Parser, Serializer}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 01/05/2016.
  * This actor handles TCP requests from the client.
  * It understands the query, sending it to the Main and giving the client the answer.
  */
class Usermanager extends ReplyActor {
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
  val builder = new ByteStringBuilder()

  /**
    * Processes all incoming messages.
    * It handles Received message buffering incoming packets,
    * parsings it and sending the message to the Main actor.
    *
    * @see Helper
    * @see #parseQuery(String)
    * @see #replyToClient(String)
    */
  def receive = {
    // When it receive data
    case Received(data: ByteString) => receiveData(data)
    // When a client disconnects
    case PeerClosed => {
      log.info("Client disconnected")
      context stop self
    }
  }

  /**
    * Buffers the bytes coming from the client and
    * checks if the message is in the correct form.
    *
    * @param data The bytes coming from the client.
    */
  def receiveData(data: ByteString): Unit = {
    builder.putBytes(data.toArray)
    var message = builder.result()
    // If the message has at least 8 bytes
    if (message.length > 8) {
      // If the message starts with 01 and ends with 02,
      // It's a complete message
      if (message(0) == 0 &&
        message(1) == 1 &&
        message(message.length - 2) == 0 &&
        message(message.length - 1) == 2) {
        // Remove starting and ending bytes
        message = message.drop(2)
        message = message.dropRight(2)
        // Gets the length of the query and removes the 4 bytes
        val lengthBytes = new Array[Byte](4)
        message.copyToArray(lengthBytes, 0, 4)
        val length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.LITTLE_ENDIAN).getInt()
        message = message.drop(4)
        // If the length is equal to the remaining message length less the operation byte
        if (length == message.length - 1)
          processRequest(message)
      }
    }
  }

  /**
    * Processes the bytes coming from the client.
    *
    * @param request The bytes request from the client.
    */
  def processRequest(request: ByteString): Unit = {
    builder.clear()
    // Gets the operation type
    val operation = request(0)
    // Gets the body of the request as a String
    val req = new String(request.drop(1).toArray)
    operation match {
      // If  the byte is equals to 1 the command it's a query
      case 1 => parseQuery(req)
      case _ => replyToClient("Invalid command")
    }
  }

  /**
    * Uses the Parser class to parse the string to a message.
    * If the message is an InvalidQueryMessage message it replies to the client,
    * otherwise it handles it.
    *
    * @param query The query string.
    * @see Parser
    * @see InvalidQueryMessage
    * @see QueryMessage
    * @see #handleQueryMessage(QueryMessage)
    */
  private def parseQuery(query: String): Unit = {
    // Use the parser to parse the string
    val message = parser.parseQuery(query)
    message match {
      // If the user command is not a valid query
      case m: InvalidQueryMessage => replyToClient("Invalid query")
      // If the user command is a valid query
      case m: QueryMessage => handleQueryMessage(m)
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString, currentMethodName()))
    }
  }

  /**
    * Handles QueryMessage messages, if it's a LoginMessage message it handles it,
    * otherwise it sends it to the Main actor.
    *
    * @param message The QueryMessage message to process.
    * @see LoginMessage
    * @see QueryMessage
    */
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
            case Success(result) => replyToClient(replyBuilder.buildReply(result.asInstanceOf[ReplyMessage]), origSender)
            case Failure(t) => log.error("Error sending message: " + t.getMessage)
          }
        }
        // If someone sends a message before login
        else replyToClient("WTF?! You can't be here if you're using our client, " +
          "so FUCK YOU and you're shitty home made actor-fuckin'-base client!")
      }
    }
  }

  /**
    * Logs in the user if the user is not connected already.
    * Checks if the given username is in the users list and if the password is the right one.
    * It replies to the client the result of the operation.
    *
    * @param username The user's username.
    * @param password The user's password.
    * @see #replyToClient(String, ActorRef)
    */
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
        if (username == "admin") mainActor = context.actorOf(Props(new Main()))
        // If the user is not 'admin' the main receive the user's permissions
        else {
          var singleUserPermission: util.HashMap[String, EnumPermission.UserPermission] =
            new util.HashMap[String, EnumPermission.UserPermission]()
          val serializer: Serializer = new Serializer
          val sm = StoremanagersRefs.refs.get("master")
          val origSender = sender
          val future = sm ? StorefinderRowMessage("permissions", new FindRowMessage(username))
          future.onComplete {
            case Success(result) => {
              val reply = result.asInstanceOf[ReplyMessage]
              reply.result match {
                case EnumReplyResult.Done => {
                  val array = reply.info.asInstanceOf[FindInfo].value
                  singleUserPermission =
                    serializer.deserialize(array).asInstanceOf[util.HashMap[String, UserPermission]]
                }
                case EnumReplyResult.Error => {
                  reply.info.asInstanceOf[KeyAlreadyExistInfo]
                }
              }
            }
            case Failure(t) => {
              log.error("Error sending message: " + t.getMessage);
              reply(new ReplyMessage(EnumReplyResult.Error, new FindRowMessage(username),
                new ServiceErrorInfo("Error sending message: " + t.getMessage)), origSender)
            }
          }
          mainActor = context.actorOf(Props(new Main(singleUserPermission)))
        }
        replyToClient("Y")
        log.info(username + " is connected")
      }
      else replyToClient("N")
    }
    else replyToClient("N")
  }

  /**
    * Sends the reply message to the original sender.
    *
    * @param reply The string to send to the client.
    * @param sender The sender of the request.
    */
  private def replyToClient(reply: String, sender: ActorRef = sender): Unit = {
    sender ! Write(ByteString(reply))
  }
}
