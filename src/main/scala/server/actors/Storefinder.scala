package server.actors

import akka.remote.RemoteScope
import server.enums.EnumReplyResult._
import server.messages.query.ReplyMessage

import scala.language.postfixOps
import java.util.concurrent.ConcurrentHashMap

import akka.actor._
import server.messages.query.user.RowMessages._

import scala.collection.JavaConversions._
import scala.util.matching.Regex
import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map */
class Storefinder extends ReplyActor {

  import akka.pattern.ask

  var storekeepers = new ConcurrentHashMap[Regex, ActorRef]()
//  storekeepers.put(".*".r, context.actorOf(Props(new Storekeeper(true)))) // Startup storekeeper
  storekeepers.put(".*".r, context.actorOf(Props(new Storekeeper(true)).withDeploy(Deploy(scope = RemoteScope(nextAddress))))) // Startup storekeeper

  /** Main receive method */
  def receive = {
    // StoreFinder should receive and handle only RowMessages
    case m:RowMessage => handleRowMessage(m)
    // If other is received it is logged as an error
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
  }

  /** Handles the messages of type RowMessage */
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

  /** Sends a RowMessage to the Storekeeper that could contain the key */
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

  /** Finds the storekeeper that could contain the key */
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
    // if no match is found return null
    null
  }
}
