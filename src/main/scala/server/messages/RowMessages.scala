package server.messages

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
trait RowMessage extends ActorbaseMessage {}

case class InsertRowMessage(key: String, value: Array[Byte]) extends RowMessage
case class UpdateRowMessage(key: String, value: Array[Byte]) extends RowMessage
case class RemoveRowMessage(key: String) extends RowMessage
case class FindRowMessage(key: String) extends RowMessage

case class StorefinderRowMessage(mapName: String, rowMessage: RowMessage) extends ActorbaseMessage