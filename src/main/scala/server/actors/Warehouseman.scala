package server.actors

import java.util

import akka.actor.{Actor, ActorLogging, ActorRef}
import server.enums.EnumReplyResult
import server.messages.internal.ScalabilityMessages._
import server.messages.internal.StorageMessages._
import server.messages.query.ReplyMessage
import server.messages.query.user.RowMessages.{InsertRowMessage, RemoveRowMessage, RowMessage, UpdateRowMessage}
import server.utils.FileManager
import server.utils.fileManagerLibrary.SingleFileManager

/**
  * Created by lucan on 11/05/2016.
  * Manages the filesystem writing and reading on the sisk.
  *
  * @constructor create a new Warehouseman actor instance from a String.
  * @param path The map's saving path.
  */
class Warehouseman(path : String) extends ReplyActor {

  val valueFilePath : String = ""

  // The object to interact with the filesystem
  val fileManager: FileManager = new SingleFileManager(path,valueFilePath)

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
        //TODO
      }
      // If the storemanager send an update message
      case UpdateRowMessage(key: String,  value: Array[Byte]) => {
        ///TODO
      }
      // If the storemanager send a remove message
      case RemoveRowMessage(key: String) => {
        //TODO
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,"handleRowMessages"))
    }
  }
}
