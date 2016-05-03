package server.messages

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
trait MapMessage extends ActorbaseMessage{}

case class CreateMapMessage(name: String) extends MapMessage
case class DeleteMapMessage(name: String) extends MapMessage
case class SelectMapMessage(name: String) extends MapMessage
case class ListMapMessage() extends MapMessage