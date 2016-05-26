package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef}
import server.enums.EnumReplyResult._
import server.messages.internal.BecomeStorekeeperMessage
import server.messages.query.ReplyMessage
import server.messages.query.user.RowMessages._


import scala.collection.JavaConversions._

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map partition */
class Storekeeper(isStorekeeper: Boolean = false) extends ReplyActor {
  var db = new ConcurrentHashMap[String, String]()

  import context._

  override def preStart(): Unit = {
    if(isStorekeeper) become(receiveAsStorekeeper)
  }
  // Row level commands
  def receive = {
    case BecomeStorekeeperMessage => become(receiveAsStorekeeper)
    case m: RowMessage => handleRowMessagesAsNinja(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
  }

  private def receiveAsStorekeeper: Receive = {
    case m: RowMessage => handleRowMessage(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
  }

  private def handleRowMessage(message: RowMessage): Unit = {
    message match {
      case InsertRowMessage(key: String, value: String) => {
        if (db.containsKey(key)) {
          reply(ReplyMessage(Error,message,KeyAlreadyExistInfo()))
        } else {
          db.put(key, value)
          logAndReply(ReplyMessage(Done, message))
        }
      }
      case UpdateRowMessage(key: String, value: String) => {
        if (!db.containsKey(key)) {
          reply(ReplyMessage(Error,message,KeyDoesNotExistInfo()))
        } else {
          db.put(key, value)
          logAndReply(ReplyMessage(Done,message))
        }
      }
      case RemoveRowMessage(key: String) => {
        if (!db.containsKey(key)) {
          reply(ReplyMessage(Error,message,KeyDoesNotExistInfo()))
        } else {
          db.remove(key)
          logAndReply(ReplyMessage(Done,message))
        }
      }
      case FindRowMessage(key: String) => {
        if (!db.containsKey(key))
          reply(ReplyMessage(Error,message,KeyDoesNotExistInfo()))
        else
          reply(ReplyMessage(Done,message))
      }
      case ListKeysMessage() => {
        val keys = List()
        for (k: String <- db.keys())
          keys.add(k)
        reply(ReplyMessage(Done,message,ListKeyInfo(keys)))
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
    }
  }

  private def handleRowMessagesAsNinja(message: RowMessage): Unit = {
    message match {
      case InsertRowMessage(key: String, value: String) => {
        if (db.containsKey(key)) return
        db.put(key, value)
        writeLog(ReplyMessage(Done,message))
      }
      case UpdateRowMessage(key: String, value: String) => {
        if (db.containsKey(key)) return
        db.put(key, value)
        writeLog(ReplyMessage(Done,message))
      }
      case RemoveRowMessage(key: String) => {
        if (!db.containsKey(key)) return
        db.remove(key)
        writeLog(ReplyMessage(Done,message))
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
    }
  }


}
