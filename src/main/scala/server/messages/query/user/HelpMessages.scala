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

package server.messages.query.user

import server.messages.query.PermissionMessages.{NoPermissionMessage}
import server.messages.query.ReplyInfo


/**
  * HelpMessages are used to manage help operations.
  */
object HelpMessages {

  /**
    * Trait that every message which belongs to help operations has to extend.
    *
    * @see UserMessage
    */
  trait HelpMessage extends UserMessage

  /**
    * A CompleteHelpMessage is used to request the complete help of Actorbase. An user does not need a particular
    * permission to request this operation, therefore this message extends NoPermissionMessage.
    *
    * @see HelpMessage
    * @see NoPermissionMessage
    */
  case class CompleteHelpMessage() extends HelpMessage with NoPermissionMessage

  /**
    * A SpecificHelpMessage is used to request the help for a specific command. An user does not need a particular
    * permission to request this operation, therefore this message extends NoPermissionMessage.
    * @param command The command the user wants to receive help of
    *
    * @see HelpMessage
    * @see NoPermissionMessage
    */
  case class SpecificHelpMessage(command: String) extends HelpMessage with NoPermissionMessage

  /**
    * A CompleteHelpReplyInfo is used as response to a request of complete help.
    * @param commands The complete help of Actorbase
    *
    * @see ReplyInfo
    */
  case class CompleteHelpReplyInfo(commands:String) extends ReplyInfo

  /**
    * A SpecificHelpReplyInfo is used as response to a request of specific help.
    * @param command The specific help for the command
    *
    * @see ReplyInfo
    */
  case class SpecificHelpReplyInfo(command: String) extends ReplyInfo
}
