package server.messages.query

import server.messages.query.PermissionMessages.{NoPermissionMessage, ReadMessage}
import server.messages.query.user.UserMessage

/**
  * Created by DavideTommasin on 11/05/16.
  */
object HelpMessages {
  trait HelpMessage extends UserMessage
  case class CompleteHelp() extends HelpMessage with NoPermissionMessage
  case class SpecificHelp(command: String) extends HelpMessage with NoPermissionMessage

  case class CompleteHelpReplyInfo(commands:String) extends ReplyInfo
  case class SpecificHelpReplyInfo(command: String) extends ReplyInfo
}
