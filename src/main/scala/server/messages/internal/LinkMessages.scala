package server.messages.internal

import akka.actor.ActorRef

/**
  * Created by lucan on 11/05/2016.
  */
object LinkMessages {
  trait LinkMessage
  case class AddNinjaMessage(ref : ActorRef) extends LinkMessage
  case class AddWarehousemanMessage(ref : ActorRef) extends LinkMessage
  case class RemoveNinjaMessage(ref : ActorRef) extends LinkMessage
  case class RemoveWarehousemanMessage(ref : ActorRef) extends LinkMessage

}
