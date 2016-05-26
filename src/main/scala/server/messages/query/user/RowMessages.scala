package server.messages.query.user

import server.messages.query.PermissionMessages.{ReadMessage, ReadWriteMessage}
import server.messages.query.ReplyInfo

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
object RowMessages {
  trait RowMessage extends UserMessage
  case class InsertRowMessage(key: String, value: String) extends RowMessage with ReadWriteMessage
  case class UpdateRowMessage(key: String, value: String) extends RowMessage with ReadWriteMessage
  case class RemoveRowMessage(key: String) extends RowMessage with ReadWriteMessage
  case class FindRowMessage(key: String) extends RowMessage with ReadMessage
  case class ListKeysMessage() extends RowMessage with ReadMessage
  case class StorefinderRowMessage(mapName: String, rowMessage: RowMessage) extends RowMessage // Message from storemanger to storefinder

  case class KeyAlreadyExistInfo() extends ReplyInfo
  case class KeyDoesNotExistInfo() extends ReplyInfo
  case class ListKeyInfo(keys: List[String]) extends ReplyInfo
  case class NoKeyInfo() extends ReplyInfo
  case class FindInfo() extends ReplyInfo
}