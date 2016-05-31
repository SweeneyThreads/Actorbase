package server.actors

import java.util

import akka.actor.{Actor, ActorLogging, ActorRef}
import server.enums.EnumReplyResult
import server.messages.internal.ScalabilityMessages.{ReadMapMessage, SendMapMessage, WriteMapMessage}
import server.messages.query.ReplyMessage
import server.messages.query.user.RowMessages.{InsertRowMessage, RemoveRowMessage, RowMessage, UpdateRowMessage}

/**
  * Created by lucan on 11/05/2016.
  */
class Warehouseman(file : String) extends ReplyActor {

  def receive = {
    // If it's a row level message
    case m: RowMessage => handleRowMessages(m)
    // If the storefinder send a write map message
    case WriteMapMessage(map: util.HashMap[String, Array[Byte]]) => {
      //TODO
    }
    // If the storefinder send a read map message
    case ReadMapMessage() => {
      //TODO
    }
    case SendMapMessage(map: util.HashMap[String, Array[Byte]], actorRef: ActorRef) => {
      //TODO
    }
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, currentMethodName()))
  }

  /** Handle row level messages (Ninja behaviour) */
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
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
    }
  }
}
