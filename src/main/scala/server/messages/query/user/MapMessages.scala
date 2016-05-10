package server.messages.query.user

import server.messages.query.PermissionMessages.{ReadMessage, ReadWriteMessage}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

object MapMessages {
  trait MapMessage extends UserMessage
  case class CreateMapMessage(name: String) extends MapMessage with ReadWriteMessage
  case class DeleteMapMessage(name: String) extends MapMessage with ReadWriteMessage
  case class SelectMapMessage(name: String) extends MapMessage with ReadMessage
  case class ListMapMessage() extends MapMessage with ReadMessage
}