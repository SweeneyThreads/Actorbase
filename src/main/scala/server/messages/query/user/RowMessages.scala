package server.messages.query.user

import server.messages.ActorbaseMessage
import server.messages.query.PermissionMessages.{ReadMessage, ReadWriteMessage}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
object RowMessages {
  trait RowMessage extends UserMessage
  case class InsertRowMessage(key: String, value: String) extends RowMessage with ReadWriteMessage
  case class UpdateRowMessage(key: String, value: String) extends RowMessage with ReadWriteMessage
  case class RemoveRowMessage(key: String) extends RowMessage with ReadWriteMessage
  case class FindRowMessage(key: String) extends RowMessage with ReadMessage
  case class ListKeysMessage() extends RowMessage with ReadMessage  // Message from storemanger to storefinder
  case class StorefinderRowMessage(mapName: String, rowMessage: RowMessage) extends ActorbaseMessage
}