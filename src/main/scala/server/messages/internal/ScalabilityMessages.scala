package server.messages.internal

import akka.actor.ActorRef

import scala.collection.immutable.HashMap

/**
  * Created by borto on 31/05/2016.
  */

object ScalabilityMessages {
  case class WriteMapMessage (map: HashMap[String, Array[Byte]])
  case class SendMapMessage (map: HashMap[String, Array[Byte]], actorRef: ActorRef)
  case class ReadMapMessage ()
}
