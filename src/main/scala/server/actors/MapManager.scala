package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, Deploy, Props}
import server.StaticSettings
import server.enums.EnumReplyResult
import server.messages.internal.AskMessages.AskMapMessage
import server.messages.query.ReplyMessage
import server.messages.query.user.MapMessages.{DeleteMapMessage, MapAlreadyExistInfo, MapDoesNotExistInfo, _}
import server.messages.query.user.RowMessages.{RowMessage, StorefinderRowMessage}

import akka.pattern.ask
import akka.remote.RemoteScope

import scala.util.{Failure, Success}
import scala.collection.JavaConversions._

/**
  * Created by matteobortolazzo on 04/06/2016.
  */
class MapManager(database: String) extends ReplyActor {
  // Registers itself to the list of MapManager actors
  StaticSettings.mapManagerRefs.put(database, self)
  // The list of storefinders
  val indexManagers = new ConcurrentHashMap[String, ActorRef]()
  // Add a default map (Storefinder). If the present Soremanager represents the Master database, it
  // does not create the default map, instead it creates the users map and the permissions map
  if(database == "master") {
    indexManagers.put("users",       context.actorOf(Props(new IndexManager(1)).withDeploy(Deploy(scope = RemoteScope(nextAddress))), name="users"))
    indexManagers.put("permissions", context.actorOf(Props(new IndexManager(1)).withDeploy(Deploy(scope = RemoteScope(nextAddress))), name="permissions"))
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
    case AskMapMessage(mapName:String) => Some(sender).map(_ ! indexManagers.containsKey(mapName))
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
        for (k: String <- indexManagers.keySet()) maps = maps.::(k)
        // If the map is empty reply with an error
        if (maps.isEmpty) reply(ReplyMessage(EnumReplyResult.Error, message, NoMapInfo()))
        // If the map is not empty
        else reply(ReplyMessage(EnumReplyResult.Done, message, ListMapInfo(maps)))
      }
      // If the user types 'createmap <map_name>'
      case CreateMapMessage(name: String) => {
        // Get the storefinder
        val sf = indexManagers.get(name)
        // If the storefinder already exists
        if (sf != null) reply(ReplyMessage(EnumReplyResult.Error, message, MapAlreadyExistInfo()))
        // If the storefinder doesn't exists
        else {
          // Add the storefinder
          indexManagers.put(name, context.actorOf(Props(new IndexManager(1)).withDeploy(Deploy(scope = RemoteScope(nextAddress)))))
          logAndReply(ReplyMessage(EnumReplyResult.Done, message))
        }
      }
      // If the user types 'deletemap <map_name>'
      case DeleteMapMessage(name: String) => {
        // Get the storefinder
        val sf = indexManagers.get(name)
        if (sf == null) reply(ReplyMessage(EnumReplyResult.Error, message, MapDoesNotExistInfo()))
        // If the storefinder doesn't exists
        else {
          // Remove the storefinder
          indexManagers.remove(name)
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
        val sf = indexManagers.get(m.mapName)
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
