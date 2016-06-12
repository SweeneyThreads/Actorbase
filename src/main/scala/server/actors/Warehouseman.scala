package server.actors

import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorLogging, ActorRef}
import server.StaticSettings
import server.enums.EnumReplyResult
import server.messages.internal.ScalabilityMessages._
import server.messages.internal.StorageMessages._
import server.messages.query.ReplyMessage
import server.messages.query.user.RowMessages.{InsertRowMessage, RemoveRowMessage, RowMessage, UpdateRowMessage}
import server.utils.{Serializer, FileManager}
import server.utils.fileManagerLibrary.SingleFileManager
import scala.language.postfixOps

/**
  * Created by lucan on 11/05/2016.
  * Manages the filesystem writing and reading on the sisk.
  *
  * @constructor create a new Warehouseman actor instance from a String.
  * @param dbName The database name.
  * @param mapName The map name.
  */
class Warehouseman(dbName: String, mapName: String) extends ReplyActor {

  println(s"Warehouseman created. dbName=$dbName, mapName=$mapName")

  // The object to interact with the filesystem
  val fileManager: FileManager = new SingleFileManager(dbName,mapName)

  /**
    * Processes all incoming messages.
    * Handles RowMessage messages and ScalabilityMessage messages.
    *
    * @see RowMessage
    * @see WriteMapMessage
    * @see ReadMapMessage
    * @see SendMapMessage
    * @see #handleRowMessages(RowMessage)
    */
  def receive = {
    // If it's a row level message
    case ReadMapMessage => giveMap(sender)
    case m: RowMessage => handleRowMessages(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, "receive"))
  }

  /**
    * Processes only RowMessage messages.
    * Handles InsertRowMessage messages adding the entry in the file.
    * Handles UpdateRowMessage messages updating the entry in the file.
    * Handles RemoveRowMessage messages removing the entry from the file.
    *
    * @param message The RowMessage message to precess.
    * @see RowMessage
    * @see InsertRowMessage
    * @see UpdateRowMessage
    * @see RemoveRowMessage
    * @see FileManager
    */
  private def handleRowMessages(message: RowMessage): Unit = {
    message match {
      // If the storemanager send an insert message
      case InsertRowMessage(key: String,  value: Array[Byte]) => {
        fileManager.InsertEntry(key,value)
      }
      // If the storemanager send an update message
      case UpdateRowMessage(key: String,  value: Array[Byte]) => {
        fileManager.UpdateEntry(key,value)
      }
      // If the storemanager send a remove message
      case RemoveRowMessage(key: String) => {
        fileManager.RemoveEntry(key)
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,"handleRowMessages"))
    }
  }

  private def giveMap(ar: ActorRef): Unit = {
    val storedMap =  fileManager.ReadMap()
    printMap(storedMap)
    ar ! ReadMapReply(storedMap)
  }


  private def printMap(map: ConcurrentHashMap[String,Array[Byte]]): Unit = {
    val i = map.keySet.iterator
    println("CULOCULOCULO")
    while (i hasNext) {
      val k = i.next
      val value = new String(map get k)
      println(s"$k => $value")
    }
  }
}
