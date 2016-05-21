package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef}
import server.messages.internal.BecomeStorekeeperMessage
import server.messages.query.user.RowMessages._

import scala.collection.JavaConversions._

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map partition */
class Storekeeper(isStorekeeper: Boolean = false) extends Actor with akka.actor.ActorLogging {
  var db = new ConcurrentHashMap[String, String]()
  val unhandledMessage = "Unhandled message in storefinder "

  import context._

  override def preStart(): Unit = {
    if(isStorekeeper) become(receiveAsStorekeeper)
  }
  // Row level commands
  def receive = {
    case BecomeStorekeeperMessage => become(receiveAsStorekeeper)
    case m: RowMessage => handleRowMessagesAsNinja(m)
    case other => log.error(unhandledMessage + ", receive (Ninja): " + other)
  }

  private def receiveAsStorekeeper: Receive = {
    case m: RowMessage => handleRowMessage(m)
    case other => log.error(unhandledMessage + ", receive: " + other)
  }

  private def handleRowMessage(message: RowMessage): Unit = {
    message match {
      case InsertRowMessage(key: String, value: String) => {
        if (db.containsKey(key)) {
          reply(key + " already exist")
          return
        }
        db.put(key, value)
        logAndReply(key + " inserted")
      }
      case UpdateRowMessage(key: String, value: String) => {
        if (!exists(key)) return
        db.put(key, value)
        logAndReply(key + " updated")
      }
      case RemoveRowMessage(key: String) => {
        if (!exists(key)) return
        db.remove(key)
        logAndReply(key + " removed")
      }
      case FindRowMessage(key: String) => {
        if (!exists(key)) return
        reply(db.get(key))
      }
      case ListKeysMessage() => {
        var keys = ""
        for (k: String <- db.keys())
          keys += k + "\n"
        reply(keys)
      }
      case _ => log.error(unhandledMessage + ", handleRowMessage: " + message)
    }
  }

  private def handleRowMessagesAsNinja(message: RowMessage): Unit = {
    message match {
      case InsertRowMessage(key: String, value: String) => {
        if (db.containsKey(key)) {
          log.warning(key + " already exist")
          return
        }
        db.put(key, value)
        log.info(key + " inserted")
      }
      case UpdateRowMessage(key: String, value: String) => {
        if (exists(key)) return
        db.put(key, value)
        log.info(key + " updated")
      }
      case RemoveRowMessage(key: String) => {
        if (!exists(key)) return
        db.remove(key)
        log.info(key + " removed")
      }
      case _ => log.error(unhandledMessage + ", handleRowMessagesAsNinja: " + message)
    }
  }

  private def exists(key: String): Boolean = {
    val ris = db.containsKey(key)
    if (!ris) reply(key + " doesn't exist")
    return ris
  }

  private def logAndReply(str: String, sender: ActorRef = sender): Unit = {
    log.info(str)
    reply(str, sender)
  }

  private def reply(str: String, sender: ActorRef = sender): Unit = {
    Some(sender).map(_ ! str)
  }
}
