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
  * Manages the filesystem writing and reading on the sisk.
  *
  * @constructor create a new Warehouseman actor instance from a String.
  * @param dbName The database name.
  * @param mapName The map name.
  */
class Warehouseman(dbName: String, mapName: String) extends ReplyActor {

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
    * @param message The RowMessage message to process.
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
    ar ! ReadMapReply(storedMap)
  }

}
