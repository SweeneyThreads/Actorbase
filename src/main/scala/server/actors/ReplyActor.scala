package server.actors

import akka.actor.ActorRef
import server.messages.query.ReplyMessage
import server.utils.ReplyBuilder

/**
  * Created by matteobortolazzo on 26/05/2016.
  * Interface that gives an actor the ability to reply and log.
  */
trait ReplyActor extends ClusterAwareActor with akka.actor.ActorLogging {

  val replyBuilder = new ReplyBuilder()

  /**
    * Logs the operation represent by the ReplyMessage message and sends the ReplyMessage to the sender.
    *
    * @param reply The ReplyMessage message that represent the reply to a request.
    * @param sender The sender's reference of the request.
    *
    * @see #reply(ReplyMessage, ActorRef)
    * @see #writeLog(ReplyMessage)
    */
  def logAndReply(reply: ReplyMessage, sender: ActorRef = sender): Unit = {
    writeLog(reply)
    this.reply(reply, sender)
  }

  /**
    * Send the ReplyMessage message to the sender.
    *
    * @param reply The ReplyMessage message that represent the reply to a request.
    * @param sender The sender's reference of the request.
    */
  def reply(reply: ReplyMessage, sender: ActorRef = sender): Unit = Some(sender).map(_ ! reply)

  /**
    * Logs the operation represent by the ReplyMessage using the ReplyBuilder class.
    *
    * @param reply The ReplyMessage message that represent the reply to a request.
    *
    * @see ReplyBuilder
    */
  def writeLog(reply: ReplyMessage): Unit = {
    log.info(replyBuilder.buildReply(reply))
  }

  /**
    * Return the name of the current running method.
    * @return The name of the method that call this.
    */
  def currentMethodName() : String = Thread.currentThread.getStackTrace()(2).getMethodName
}
