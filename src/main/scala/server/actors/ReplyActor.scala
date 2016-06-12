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

import akka.actor.ActorRef
import server.messages.query.ReplyMessage
import server.utils.ReplyBuilder

/**
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
