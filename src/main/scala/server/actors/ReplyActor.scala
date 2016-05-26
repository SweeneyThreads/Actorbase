package server.actors

import akka.actor.{Actor, ActorRef}
import server.messages.query.ReplyMessage
import server.utils.ReplyBuilder

/**
  * Created by borto on 26/05/2016.
  */
trait ReplyActor extends Actor with akka.actor.ActorLogging {

  var replyBuilder = new ReplyBuilder()

  def logAndReply(reply: ReplyMessage, sender: ActorRef = sender): Unit = {
    writeLog(reply)
    this.reply(reply, sender)
  }

  def reply(reply: ReplyMessage, sender: ActorRef = sender): Unit = Some(sender).map(_ ! reply)

  def writeLog(reply: ReplyMessage): Unit = {
    log.info(replyBuilder.buildReply(reply))
  }


  def currentMethodName() : String = Thread.currentThread.getStackTrace()(2).getMethodName

}
