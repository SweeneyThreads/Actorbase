package server.messages

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
trait RowMessage extends ActorbaseMessage

case class InsertRowMessage(key: String, value: Array[Byte]) extends RowMessage with ReadWriteMessage

case class UpdateRowMessage(key: String, value: Array[Byte]) extends RowMessage with ReadWriteMessage

case class RemoveRowMessage(key: String) extends RowMessage with ReadWriteMessage

case class FindRowMessage(key: String) extends RowMessage with ReadMessage

case class ListKeysMessage() extends RowMessage with ReadMessage

// Message from storemanger to storefinder
case class StorefinderRowMessage(mapName: String, rowMessage: RowMessage) extends ActorbaseMessage