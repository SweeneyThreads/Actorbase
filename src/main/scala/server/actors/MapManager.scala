/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 SWEeneyThreads
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 *
 * @author SWEeneyThreads
 * @version 0.0.1
 * @since 0.0.1
 */

package server.actors

import java.io.File
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, Props}
import server.StaticSettings
import server.enums.EnumReplyResult
import server.enums.EnumWarehousemanType.DatabaseWarehousemanType
import server.messages.internal.AskMessages.AskMapMessage
import server.messages.internal.WarehousemenMessages.EraseDatabaseMessage
import server.messages.query.ReplyMessage
import server.messages.query.user.DatabaseMessages.{DeleteDatabaseMessage, DatabaseMessage}
import server.messages.query.user.MapMessages.{DeleteMapMessage, MapAlreadyExistInfo, MapDoesNotExistInfo, _}
import server.messages.query.user.RowMessages.{InsertRowMessage, RowMessage, StorefinderRowMessage}

import scala.collection.JavaConversions._

/**
  * A MapManager actor represents a database of Actorbase. It manages all the maps contained in the database (represented
  * by IndexManager actors).
  */
class MapManager extends ReplyActor {
  // Registers itself to the list of MapManager actors
  StaticSettings.subscribe(self.path.name, self)

  // The list of storefinders
  val indexManagers = new ConcurrentHashMap[String, ActorRef]()
  // Add a default map (Storefinder). If the present Soremanager represents the Master database, it
  // does not create the default map, instead it creates the users map and the permissions map

  val databaseWarehouseman = context.actorOf(Props(new Warehouseman(DatabaseWarehousemanType,self.path.name)))

  /**
    *
    */
  override def preStart(): Unit = {
    val dbDirectory = new File(StaticSettings.dataPath + "\\" + self.path.name)
    if (dbDirectory.exists()) {
      val mapsDirectory = dbDirectory.listFiles()
      for (child <- mapsDirectory) {
        indexManagers.put(child.getName, context.actorOf(Props[IndexManager], name = child.getName))
      }
    } else {
      if (self.path.name == "master") {
        indexManagers.put("users", context.actorOf(Props[IndexManager], name = "users"))
        indexManagers.put("permissions", context.actorOf(Props[IndexManager], name = "permissions"))
        val actor = indexManagers.get("users")
        actor.tell(new InsertRowMessage("admin", "admin".getBytes("UTF-8")), self)
      }
    }
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
    // If it's a database level message
    case m:DatabaseMessage => handleDatabaseMessage(m)
    // If it's a map level message
    case m:MapMessage => handleMapMessage(m)
    // If it's a row level message
    case m:RowMessage => handleRowMessage(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString(), "receive"))
  }


  private def handleDatabaseMessage(m: DatabaseMessage): Unit = {
    m match {
      case DeleteDatabaseMessage(name: String) =>
        for (im<-indexManagers.keys) {
          indexManagers.get(im) ! new DeleteMapMessage(im)
        }
        databaseWarehouseman ! EraseDatabaseMessage
        context stop self
    }
  }

  /**
    * Processes only MapMessage messages.
    * Handles ListMapMessage messages returning the list of maps.
    * Handles CreateMapMessage messages creating a new Storefinder actor which represents the new map.
    * Handles DeleteMapMessage messages deleting Storefinder actors that represent the map.
    *
    * @param message The MapMessage message to process.
    * @see MapMessage
    * @see ListMapMessage
    * @see CreateMapMessage
    * @see DeleteMapMessage
    * @see ReplyMessage
    */
  private def handleMapMessage(message: MapMessage): Unit = {
    message match {
      // If the user types 'listmap'
      case ListMapMessage() => {
        if (indexManagers.isEmpty) reply(ReplyMessage(EnumReplyResult.Error, message, NoMapInfo()))
        else {
          // Create a list
          var maps = List[String]()
          // For each storekeeper adds the map name to the list
          for (k: String <- indexManagers.keySet()) maps = maps.::(k)
          // If the map is not empty
          reply(ReplyMessage(EnumReplyResult.Done, message, ListMapInfo(maps)))
        }
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
          indexManagers.put(name, context.actorOf(Props[IndexManager], name = name))
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
          sf forward message
          indexManagers.remove(name)
          logAndReply(ReplyMessage(EnumReplyResult.Done, message))
        }
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleMapMessage"))
    }
  }

  /**
    * Processes only StorefinderRowMessage messages.
    * Finds the right Storefinder actor and sends the RowMessage message to it.
    *
    * @param message The RowMessage message to process.
    * @see RowMessage
    * @see StorefinderRowMessage
    */
  private def handleRowMessage(message: RowMessage): Unit = {
    message match {
      // If it's a StorefinderRowMessage
      case m: StorefinderRowMessage => {
        // Get the storefinder
        val im = indexManagers.get(m.mapName)
        if (im == null) reply(ReplyMessage(EnumReplyResult.Error, message, MapDoesNotExistInfo()))
        // Forward the message to the IndexManager
        else im forward  m.rowMessage
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString(), "handleRowMessage"))
    }
  }
}
