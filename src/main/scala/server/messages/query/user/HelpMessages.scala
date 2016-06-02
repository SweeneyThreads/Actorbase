package server.messages.query.user

import server.messages.query.PermissionMessages.{NoPermissionMessage}
import server.messages.query.ReplyInfo

/**
  * Created by DavideTommasin on 11/05/16.
  */

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
