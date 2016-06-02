package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, Props}
import server.enums.EnumReplyResult
import server.messages.internal.AskMessages.AskMapMessage
import server.messages.query.ReplyMessage
import server.messages.query.user.MapMessages._
import server.messages.query.user.RowMessages.{RowMessage, StorefinderRowMessage}

import scala.collection.JavaConversions._
import scala.language.postfixOps
import scala.util.{Failure, Success}
import akka.dispatch.ExecutionContexts._
import akka.pattern.ask
import akka.util.Timeout
import server.StoremanagersRefs

import scala.concurrent.duration._

/**
  * Created by matteobortolazzo on 02/05/2016.
  * Actor that represent a database or part of it, it manages indexes and backups.  *
  * if it's  the 'master' database it creates the 'users' and the 'permissions' map.
  */
class Storemanager(name: String) extends ReplyActor {
  // Registers itself to the list of usermanager
  StoremanagersRefs.refs.put(name, self)
   // Values for futures
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  // The list of storefinders
  val storefinders = new ConcurrentHashMap[String, ActorRef]()
  // Add a default map (Storefinder). If the present Soremanager represents the Master database, it
  // does not create the default map, instead it creates the users map and the permissions map
  if(name == "master") {
    storefinders.put("users", context.actorOf(Props[Storefinder]))
    storefinders.put("permissions", context.actorOf(Props[Storefinder]))
  }

  /**
    * Processes all incoming messages.
    * Handles AskMapMessage messages replying if it contains the asked map.
    * Handles MapMessage and RowMessage messages.
    *
    * @see AskMapMessage
    * @see MapMessage
    * @see RowMessage
    * @see #handleMapMessage(MapMessage)
    * @see #handleRowMessage(RowMessage)
    */
  def receive = {
    // Replay to the main actor if there's a map with that name
    case AskMapMessage(mapName:String) => Some(sender).map(_ ! storefinders.containsKey(mapName))
    // If it's a map level message
    case m:MapMessage => handleMapMessage(m)
    // If it's a row level message
    case m:RowMessage => handleRowMessage(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString(), currentMethodName()))
  }

  /**
    * Processes only MapMessage messages.
    * Handles ListMapMessage messages returning the list of maps.
    * Handles CreateMapMessage messages creating a new Storefinder actor which represents the new map.
    * Handles DeleteMapMessage messages deleting Storefinder actors that represent the map.
    *
    * @param message The MapMessage message to precess.
    * @see MapMessage
    * @see ListMapMessage
    * @see CreateMapMessage
    * @see DeleteMapMessage
    * @see ReplyMessage
    */
  def handleMapMessage(message: MapMessage): Unit = {
    message match {
      // If the user types 'listmap'
      case ListMapMessage() => {
        // Create a list
        var maps = List[String]()
        // For each storekeeper adds the map name to the list
        for (k: String <- storefinders.keys()) maps = maps.::(k)
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

  /**
    * Processes only StorefinderRowMessage messages.
    * Finds the right Storefinder actor and sends the RowMessage message to it.
    *
    * @param message The RowMessage message to precess.
    * @see RowMessage
    * @see StorefinderRowMessage
    */
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
            case Success(result) => reply(result.asInstanceOf[ReplyMessage], oldSender)
            case Failure(t) => log.error("Error sending message: " + t.getMessage)
          }
        }
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), currentMethodName()))
    }
  }
}
