package server.actors

import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef}
import server.enums.EnumReplyResult
import server.enums.EnumReplyResult._
import server.messages.internal.BecomeStorekeeperMessage
import server.messages.internal.ScalabilityMessages.SendMapMessage
import server.messages.query.ReplyMessage
import server.messages.query.user.RowMessages._

import scala.collection.JavaConversions._

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map partition */
class Storekeeper(isStorekeeper: Boolean = false) extends ReplyActor {
  var db = new ConcurrentHashMap[String,  Array[Byte]]()

  import context._

  /** Before the actor starts */
  override def preStart(): Unit = {
    // If it has to behave like a Storekeeper it calls the become method
    if(isStorekeeper) become(receiveAsStorekeeper)
  }

  /** The main receive method (Ninja behavior) */
  def receive = {
    // If it's a become Storekeeper message it calls the become method and changes it's receive method
    case BecomeStorekeeperMessage => become(receiveAsStorekeeper)
    // If it's a row level message
    case m: RowMessage => handleRowMessagesAsNinja(m)
    // if the storefinder send a send map message
    case SendMapMessage(map: util.HashMap[String, Array[Byte]], actorRef: ActorRef) => {
      //TODO
    }
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, currentMethodName()))
  }

  /** The main receive method (Storekeeper behavior) */
  private def receiveAsStorekeeper: Receive = {
    // If it's a row level message
    case m: RowMessage => handleRowMessage(m)
    // if the storefinder send a send map message
    case SendMapMessage(map: util.HashMap[String, Array[Byte]], actorRef: ActorRef) => {
      //TODO
    }
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, currentMethodName()))
  }

  /** Handle row level messages (Storekeeper behaviour) */
  private def handleRowMessage(message: RowMessage): Unit = {
    message match {
      // If the user types "insert '<key>' <value>"
      case InsertRowMessage(key: String, value: Array[Byte]) => {
        // If the storekeeper already contains that key
        if (db.containsKey(key)) reply(ReplyMessage(EnumReplyResult.Error,message,KeyAlreadyExistInfo()))
        // If the storekeeper doesn't have that key
        else {
          // Insert the entry
          db.put(key, value)
          logAndReply(ReplyMessage(EnumReplyResult.Done, message))
        }
      }
      // If the user types "udpdate '<key>' <value>"
      case UpdateRowMessage(key: String, value: Array[Byte]) => {
        // If the storekeeper doesn't have that key
        if (!db.containsKey(key)) reply(ReplyMessage(EnumReplyResult.Error,message,KeyDoesNotExistInfo()))
        // If the storekeeper contains that key
        else {
          // Update the entry
          db.put(key, value)
          logAndReply(ReplyMessage(Done,message))
        }
      }
      // If the user types "remove '<key>'"
      case RemoveRowMessage(key: String) => {
        // If the storekeeper doesn't have that key
        if (!db.containsKey(key)) reply(ReplyMessage(EnumReplyResult.Error,message,KeyDoesNotExistInfo()))
        // If the storekeeper contains that key
        else {
          // Remove the entry
          db.remove(key)
          logAndReply(ReplyMessage(Done,message))
        }
      }
      // If the user types "find '<key>'"
      case FindRowMessage(key: String) => {
        // If the storekeeper doesn't have that key
        if (!db.containsKey(key)) reply(ReplyMessage(EnumReplyResult.Error,message,KeyDoesNotExistInfo()))
        // If the storekeeper contains that key
        else reply(ReplyMessage(EnumReplyResult.Done,message, FindInfo(db.get(key))))
      }
      // If the user types "listkey"
      case ListKeysMessage() => {
        // Create the list of key
        var keys = List[String]()
        // For each key, add to the list
        for (k: String <- db.keys()) keys = keys.::(k)
        // Reply with the list
        reply(ReplyMessage(EnumReplyResult.Done,message,ListKeyInfo(keys)))
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
    }
  }

  /** Handle row level messages (Ninja behaviour) */
  private def handleRowMessagesAsNinja(message: RowMessage): Unit = {
    message match {
      // If the storemanager send an insert message
      case InsertRowMessage(key: String, value: Array[Byte]) => {
        // If the key already exists
        if (db.containsKey(key)) return
        // If the key doesn't exist
        db.put(key, value)
        writeLog(ReplyMessage(EnumReplyResult.Done,message))
      }
      // If the storemanager send an update message
      case UpdateRowMessage(key: String, value: Array[Byte]) => {
        // If the key doesn't exist
        if (db.containsKey(key)) return
        // If the key exists
        db.put(key, value)
        writeLog(ReplyMessage(EnumReplyResult.Done,message))
      }
      // If the storemanager send a remove message
      case RemoveRowMessage(key: String) => {
        if (!db.containsKey(key)) return
        db.remove(key)
        writeLog(ReplyMessage(EnumReplyResult.Done,message))
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
    }
  }
}
