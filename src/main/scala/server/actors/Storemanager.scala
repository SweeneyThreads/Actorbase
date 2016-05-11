package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import server.messages.internal.AskMapMessage
import server.messages.query.user.MapMessages.{CreateMapMessage, DeleteMapMessage, ListMapMessage, MapMessage}
import server.messages.query.user.RowMessages.{RowMessage, StorefinderRowMessage}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a database */
class Storemanager extends Actor with akka.actor.ActorLogging {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  val unhandledMessage = "Unhandled message in storemanager "

  var storefinders = new ConcurrentHashMap[String, ActorRef]()
  storefinders.put("defaultMap", context.actorOf(Props[Storefinder]))

  def receive = {
    // Replay to the main actor if there's a map with that name
    case AskMapMessage(mapName:String) => Some(sender).map(_ ! storefinders.containsKey(mapName))
    case m:MapMessage => handleMapMessage(m)
    case m:RowMessage => handleRowMessage(m)
    case other => log.error(unhandledMessage + ", receive: " + other)
  }

  def handleMapMessage(message: MapMessage): Unit = {
    message match {
      case ListMapMessage() => {
        var maps = ""
        for (k: String <- storefinders.keys())
          maps += k
        if (maps == "") reply("No maps")
        else reply(maps)
      }
      case CreateMapMessage(name: String) => {
        val sf = storefinders.get(name)
        if (sf == null) {
          storefinders.put(name, context.actorOf(Props[Storefinder]))
          logAndReply("Map " + name + " created")
        }
        else reply("Map " + name + " already exists")
      }
      case DeleteMapMessage(name: String) => {
        val sf = storefinders.get(name)
        if (sf != null) {
          storefinders.remove(name)
          logAndReply("Map " + name + " deleted")
        }
        else reply("Map " + name + " doesn't exits")
      }
      case _ => log.error(unhandledMessage + ", handleMapMessage: " + message)
    }
  }

  private def handleRowMessage(message: RowMessage): Unit = {
    message match {
      case m: StorefinderRowMessage => {
        val sf = storefinders.get(m.mapName)
        if (sf != null) {
          val origSender = sender
          val future = sf ? m.rowMessage
          future.onComplete {
            case Success(result) => reply(result.toString, origSender)
            case Failure(t) => log.error("Error sending message: " + t.getMessage)
          }
        }
        else reply("Map " + m.mapName + " doesn't exist")
      }
      case _ => log.error(unhandledMessage + ", handleRowMessage: " + message)
    }
  }

  private def logAndReply(str:String, sender: ActorRef = sender): Unit = {
    log.info(str)
    reply(str, sender)
  }

  private def reply(str:String, sender: ActorRef = sender): Unit = Some(sender).map(_ ! str)
}
