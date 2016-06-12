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
  * Created by matteobortolazzo on 04/06/2016.
  */
class IndexManager() extends ReplyActor {

  // The main Storemanager actor reference
  var storemanager: ActorRef = null
  // References to all Warehouseman actors
  val warehousemen = new util.ArrayList[ActorRef]()


  override def preStart {
    println(s"IndexManager::preStart")
    // Adds Warehouseman actors
    for(i <- 0 until StaticSettings.warehousemanNumber) {
      println(s"IndexManager::createWarehousemanLoop, i=$i")
      warehousemen.add(context.actorOf(Props(new Warehouseman(context.parent.path.name,self.path.name))))
    }
    val mapDirectory = new File(StaticSettings.dataPath+"\\"+context.parent.path.name+"\\"+self.path.name)
    if (mapDirectory.exists) {
      var map: ConcurrentHashMap[String,Array[Byte]] = null
      println(s"IndexManager::message sended to the warehouseman 0")
      val future = warehousemen(0) ? ReadMapMessage
      future.onSuccess {
        case result =>
          println("IndexManager::Future received")
          val msg = result.asInstanceOf[ReadMapReply]
          map = msg.map
      }
      storemanager = context.actorOf(Props(
        new Storemanager(map, ("", null), EnumStoremanagerType.StorekeeperType)).withDeploy(Deploy(scope = RemoteScope(nextAddress))))
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
      println(other.toString)
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

