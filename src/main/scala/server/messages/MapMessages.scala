package server.messages

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
trait MapMessage extends ActorbaseMessage

case class CreateMapMessage(name: String) extends MapMessage with ReadWriteMessage

case class DeleteMapMessage(name: String) extends MapMessage with ReadWriteMessage

case class SelectMapMessage(name: String) extends MapMessage with ReadMessage

case class ListMapMessage() extends MapMessage with ReadMessage