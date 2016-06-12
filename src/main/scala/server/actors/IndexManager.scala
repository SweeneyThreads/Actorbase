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
import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.Status.Success
import akka.actor.{ActorRef, Deploy, Props}
import akka.remote.RemoteScope
import server.StaticSettings
import server.enums.EnumStoremanagerType
import server.messages.internal.StorageMessages.{ReadMapReply, ReadMapMessage}
import server.messages.query.user.RowMessages.RowMessage

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}
import akka.pattern.ask

/**
  * An IndexManager represents a map of Actorbase. It manages data in RAm and disk.
  */
class IndexManager() extends ReplyActor {

  // The main Storemanager actor reference
  var storemanager: ActorRef = null
  // References to all Warehouseman actors
  val warehousemen = new util.ArrayList[ActorRef]()


  override def preStart {
    // Adds Warehouseman actors
    for(i <- 0 until StaticSettings.warehousemanNumber) {
      warehousemen.add(context.actorOf(Props(new Warehouseman(context.parent.path.name,self.path.name))))
    }
    val mapDirectory = new File(StaticSettings.dataPath+"\\"+context.parent.path.name+"\\"+self.path.name)
    if (mapDirectory.exists) {
      val future = warehousemen(0) ? ReadMapMessage
      future.onSuccess {
        case result =>
          val msg = result.asInstanceOf[ReadMapReply]
          storemanager = context.actorOf(Props(
            new Storemanager(msg.map, ("", null), EnumStoremanagerType.StorekeeperType)).withDeploy(Deploy(scope = RemoteScope(nextAddress))))
      }
    } else {
      // The main Storemanager actor reference
      storemanager = context.actorOf(Props(
        new Storemanager(new ConcurrentHashMap[String, Array[Byte]](), ("", null), EnumStoremanagerType.StorekeeperType)).withDeploy(Deploy(scope = RemoteScope(nextAddress))))
    }
  }



  /**
    * Processes all incoming messages.
    * It handles only QueryMessage messages.
    *
    * @see QueryMessage
    * @see #handleQueryMessage(QueryMessage)
    */
  def receive ={
    case m:RowMessage => handleRowMessage(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, "receive"))
  }

  /**
    * Handles the RowMessage message sending it to the Storemanager
    * actor and to all Warehouseman actors.
    *
    * @param message The RowMessage message.
    * @see Storemanager
    * @see Warehouseman
    * @see RowMessage
    */
  private def handleRowMessage(message: RowMessage): Unit = {
    val origSender = sender
    // Forward the message to the Storemanager
    storemanager forward  message
    // Send the message to all Warehouseman actors
    for(wh <- warehousemen) wh.tell(message, self)
  }
}

