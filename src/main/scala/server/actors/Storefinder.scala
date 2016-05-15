package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import server.messages.query.user.RowMessages._

import scala.collection.JavaConversions._
import scala.util.matching.Regex
import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map */
class Storefinder extends Actor with akka.actor.ActorLogging {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  val unhandledMessage = "Unhandled message in storefinder "

  var storekeepers = new ConcurrentHashMap[Regex, ActorRef]()
  storekeepers.put(".*".r, context.actorOf(Props(new Storekeeper(true)))) // Startup storekeeper

  def receive = {
    case m:RowMessage => handleRowMessage(m)
    case other => log.error(unhandledMessage + ", receive: " + other)
  }

  private def handleRowMessage(message: RowMessage) : Unit = {
    message match {
      case ListKeysMessage() => {
        val origSender = sender
        val storeKeeperNumber = storekeepers.keys().length
        var messagesReceived = 0
        var keys = ""
        for (r: Regex <- storekeepers.keys()) {
          val future = storekeepers.get(r) ? message
          future.onComplete {
            case Success(result) => {
              messagesReceived = messagesReceived + 1
              keys += result + "\n"
              if (messagesReceived == storeKeeperNumber) {
                if (keys == "") reply("No keys in this map")
                else {
                  val res = result.toString
                  reply(res.substring(0, res.length - 1), origSender)
                }
              }
            }
            case Failure(t) => {
              messagesReceived = messagesReceived + 1
              log.error("Error sending message: " + t.getMessage)
            }
          }
        }
      }
      case InsertRowMessage(key: String, value: String) => sendToStorekeeper(key, message)
      case UpdateRowMessage(key: String, value: String) => sendToStorekeeper(key, message)
      case RemoveRowMessage(key: String) => sendToStorekeeper(key, message)
      case FindRowMessage(key: String) => sendToStorekeeper(key, message)
      case _ => log.error(unhandledMessage + ", handleRowMessage: " + message)
    }
  }

  private def sendToStorekeeper(key:String, message: RowMessage): Unit = {
    val sk = findActor(key)
    if (sk == null) {
      reply("Storefinder not found")
      return
    }
    val origSender = sender
    val future = sk ? message
    future.onComplete {
      case Success(result) => reply(result.toString, origSender)
      case Failure(t) => log.error("Error sending message: " + t.getMessage)
    }
  }

  //** Finds the storekeeper that could contain the key */
  private def findActor(key:String):ActorRef = {
    for(r:Regex <- storekeepers.keys()) {
      val m = r.findFirstIn(key)
      if(m.isDefined)
        return storekeepers.get(r)
    }
    return null
  }

  private def reply(str:String, sender: ActorRef = sender): Unit = {
    Some(sender).map(_ ! str)
  }
}
