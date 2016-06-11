package server.actors

import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, Deploy, Props}
import akka.remote.RemoteScope
import server.StaticSettings
import server.enums.EnumStoremanagerType
import server.messages.query.user.RowMessages.RowMessage

import scala.collection.JavaConversions._

/**
  * Created by matteobortolazzo on 04/06/2016.
  */
class IndexManager() extends ReplyActor {

  // The main Storemanager actor reference
  val storemanager = context.actorOf(Props(
    new Storemanager(new ConcurrentHashMap[String, Array[Byte]](), ("", null), EnumStoremanagerType.StorekeeperType)).withDeploy(Deploy(scope = RemoteScope(nextAddress))))
  // References to all Warehouseman actors
  val warehousemen = new util.ArrayList[ActorRef]()
  // Adds Warehouseman actors
  var i = 0
  for(i <- 0 to StaticSettings.warehousemanNumber) {
    warehousemen.add(context.actorOf(Props(new Warehouseman("file"))))
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
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString(), "receive"))
  }

  /**
    * Handles the RowMessage message sending it to the Storemanager
    * actor and to all Warehouseman actors.
    *
    * @param message The RowMessage message.
    *
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

