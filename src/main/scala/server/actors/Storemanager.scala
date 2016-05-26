package server.actors

import scala.language.postfixOps
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import server.enums.EnumReplyResult
import server.messages.internal.AskMapMessage
import server.messages.query.ReplyMessage
import server.messages.query.user.MapMessages._
import server.messages.query.user.RowMessages.{RowMessage, StorefinderRowMessage}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a database */
class Storemanager extends ReplyActor {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  // Values for futures
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  // The list of storefinders
  var storefinders = new ConcurrentHashMap[String, ActorRef]()
  // Add a default map (Storefinder)
  storefinders.put("defaultMap", context.actorOf(Props[Storefinder]))

  /** The main receive method */
  def receive = {
    // Replay to the main actor if there's a map with that name
    case AskMapMessage(mapName:String) => Some(sender).map(_ ! storefinders.containsKey(mapName))
    // If it's a map level message
    case m:MapMessage => handleMapMessage(m)
    // If it's a row level message
    case m:RowMessage => handleRowMessage(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString(), currentMethodName()))
  }

  /** Handle map level messages */
  def handleMapMessage(message: MapMessage): Unit = {
    message match {
      // If the user types 'listmap'
      case ListMapMessage() => {
        // Create a list
        val maps = List()
        // For each storekeeper adds the map name to the list
        for (k: String <- storefinders.keys()) maps.add(k)
        // If the map is empty reply with an error
        if (maps.isEmpty) reply(ReplyMessage(EnumReplyResult.Error, message, NoMapInfo()))
        // If the map is not empty
        else reply(ReplyMessage(EnumReplyResult.Done, message, ListMapInfo(maps)))
      }
      // If the user types 'createmap <map_name>'
      case CreateMapMessage(name: String) => {
        // Get the storefinder
        val sf = storefinders.get(name)
        // If the storefinder already exists
        if (sf != null) reply(ReplyMessage(EnumReplyResult.Error, message, MapAlreadyExistInfo()))
        // If the storefinder doesn't exists
        else {
          // Add the storefinder
          storefinders.put(name, context.actorOf(Props[Storefinder]))
          logAndReply(ReplyMessage(EnumReplyResult.Done, message))
        }
      }
      // If the user types 'deletemap <map_name>'
      case DeleteMapMessage(name: String) => {
        // Get the storefinder
        val sf = storefinders.get(name)
        if (sf == null) reply(ReplyMessage(EnumReplyResult.Error, message, MapDoesNotExistInfo()))
        // If the storefinder doesn't exists
        else {
          // Remove the storefinder
          storefinders.remove(name)
          logAndReply(ReplyMessage(EnumReplyResult.Done, message))
        }
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), currentMethodName()))
    }
  }

  /** Handle row level messages */
  private def handleRowMessage(message: RowMessage): Unit = {
    message match {
      // If it's a StorefinderRowMessage
      case m: StorefinderRowMessage => {
        // Get the storefinder
        val sf = storefinders.get(m.mapName)
        if (sf == null) reply(ReplyMessage(EnumReplyResult.Error, message, MapDoesNotExistInfo()))
        else {
          // Save the original sender
          val oldSender = sender
          // Send the message to the storefinder and save the reply in a future
          val future = sf ? m.rowMessage
          future.onComplete {
            // Reply the storemanager with the reply from the storekeeper
            case Success(result) => reply(result.asInstanceOf[ReplyMessage])
            case Failure(t) => log.error("Error sending message: " + t.getMessage)
          }
        }
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), currentMethodName()))
    }
  }
}
