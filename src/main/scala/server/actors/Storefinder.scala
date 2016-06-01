package server.actors

import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, Props}
import server.enums.EnumReplyResult._
import server.messages.query.ReplyMessage
import server.messages.query.user.RowMessages._

import scala.collection.JavaConversions._
import scala.language.postfixOps
import scala.util.matching.Regex
import scala.util.{Failure, Success}
import akka.dispatch.ExecutionContexts._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._


/**
  * Created by matteobortolazzo on 02/05/2016.
  * Actor that represent a map or part of it, it manages indexes and backups.
  */
class Storefinder extends ReplyActor {

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  val storekeepers = new ConcurrentHashMap[Regex, ActorRef]()
  storekeepers.put(".*".r, context.actorOf(Props(new Storekeeper(true)))) // Startup storekeeper
  val ninjas = new ConcurrentHashMap[ActorRef, util.ArrayList[ActorRef]]()
  val warehousemans = new ConcurrentHashMap[ActorRef, util.ArrayList[ActorRef]]()

  /**
    * Processes all incoming messages.
    * It handles only RowMessage messages
    *
    * @see #handleRowMessage(RowMessage)
    */
  def receive = {
    // StoreFinder should receive and handle only RowMessages
    case m:RowMessage => handleRowMessage(m)
    // If other is received it is logged as an error
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
  }

  /**
    * Processes RowMessage messages.
    * Handles ListKeysMessage messages asking to every Storekeeper actor the list of keys
    * and returning the complete list.
    * All other RowMessage messages are sent to the right Storekeeper actor.
    *
    * @param message The RowMessage message to precess.
    *
    * @see #sendToStorekeeper(String, RowMessage)
    * @see RowMessage
    * @see ListKeysMessage
    * @see InsertRowMessage
    * @see UpdateRowMessage
    * @see FindRowMessage
    * @see Storekeeper
    */
  private def handleRowMessage(message: RowMessage) : Unit = {
    message match {
      // if the user types 'keys'
      case ListKeysMessage() => {
        // save the original sender
        val origSender = sender
        // the number of storekeepers of the map, taken from the length of 'storekeepers' map
        val storeKeeperNumber = storekeepers.keys().length
        // messages received back from storekeepers, initially 0
        var messagesReceived = 0
        //  list used to save all the keys of the map. initially Empty
        var keys = List[String]()
        // for every storekeeper in 'storekeeper' map
        for (sk: ActorRef <- storekeepers.values()) {
          // send the message to the storekeeper and save the reply in a Future
          val future = sk ? message
          // when the reply arrives
          future.onComplete {
            // if it's successful
            case Success(result) => {
              // increment the number of messages returned
              messagesReceived = messagesReceived + 1
              // the reply of a storekeeper contains the list of his keys(as a ListKeyInfo) in info field.
              val ris = result.asInstanceOf[ReplyMessage].info.asInstanceOf[ListKeyInfo].keys
              // if this partial list is not empty it's added to the main list
              if(ris.nonEmpty) keys = keys ::: ris
              // if messages received back is equal to the number of storekeeper the main list should be complete
              if (messagesReceived == storeKeeperNumber) {
                // if the main list is empty the reply to the sender is an "Error"
                if (keys.isEmpty) reply(ReplyMessage(Error, message, NoKeyInfo()), origSender)
                // if the main list is not empty the reply to the sender is "Done" and the list id returned as a ListKeyInfo
                // keys list can be unsorted, cause the order of storekeeper's response is unknown. So keys is sorted before the reply
                else reply(ReplyMessage(Done, message, ListKeyInfo(keys.sorted)), origSender)
              }
            }
            // NB: the method returns the keys he manage to get. If a Failure occurs it is logged, but the user is not informed.
            case Failure(t) => {
              // messages are incremented to complete the loop
              messagesReceived = messagesReceived + 1
              // the error is logged
              log.error("Error sending message: " + t.getMessage)
              // if messages received back is equal to the number of storekeeper the main list should be complete
              if (messagesReceived == storeKeeperNumber) {
                // if the main list is empty the reply to the sender is an "Error"
                if (keys.isEmpty) reply(ReplyMessage(Error, message, NoKeyInfo()), origSender)
                // if the main list is not empty the reply to the sender is "Done" and the list id returned as a ListKeyInfo
                // keys list can be unsorted, cause the order of storekeeper's response is unknown. So keys is sorted before the reply
                else reply(ReplyMessage(Done, message, ListKeyInfo(keys.sorted)), origSender)
              }
            }
          }
        }
      }
      // if the message type is InsertRowMessage, forward it to the storekeeper
      case InsertRowMessage(key: String, value: Array[Byte]) => sendToStorekeeper(key, message)
      // if the message type is UpdateRowMessage, forward it to the storekeeper
      case UpdateRowMessage(key: String, value: Array[Byte]) => sendToStorekeeper(key, message)
      // if the message type is RemoveRowMessage, forward it to the storekeeper
      case RemoveRowMessage(key: String) => sendToStorekeeper(key, message)
      // if the message type is FindRowMessage, forward it to the storekeeper
      case FindRowMessage(key: String) => sendToStorekeeper(key, message)
      // if the message type is not a RowMessage, log an error
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
    }
  }

  /**
    * Sends the RowMessage message to the right Storekeeper actor using the findActor method.
    *
    * @param key The key of the entry.
    * @param message The message to send
    *
    * @see #findActor(String)
    * @see RowMessage
    * @see Storekeeper
    */
  private def sendToStorekeeper(key:String, message: RowMessage): Unit = {
    // storekeeper that could contain the key
    val sk = findActor(key)
    // save the original sender
    val origSender = sender
    // send the message to the storekeeper and save the reply in a Future
    val future = sk ? message
    future.onComplete {
      // reply the storemanager with the reply from the storekeeper
      case Success(result) => reply(result.asInstanceOf[ReplyMessage], origSender)
      case Failure(t) => log.error("Error sending message: " + t.getMessage)
    }
  }

  /**
    * Finds the Storekeeper actor which manages the group of keys the given key should be in.
    *
    * @param key The key of the entry.
    * @return The reference of the Storekeeper which manages the group of keys the given key should be in.
    */
  private def findActor(key:String):ActorRef = {
    // for every storekeeper in the map
    for(r:Regex <- storekeepers.keys()) {
      // find a match
      val m = r.findFirstIn(key)
      // if there's a match
      if(m.isDefined)
        // return the reference to the storekeeper
        return storekeepers.get(r)
    }
    // if no match is found return null (it should not arrives here)
    null
  }
}
