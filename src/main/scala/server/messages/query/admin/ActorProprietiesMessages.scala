package server.messages.query.admin

/**
  * Created by lucan on 10/05/2016.
  */
object ActorProprietiesMessages {
  trait ActorPropriertiesMessage extends AdminMessage
  case class MaxRowsMessage(number : Integer) extends ActorPropriertiesMessage
  case class MaxStorekeeper(number : Integer) extends ActorPropriertiesMessage
  case class MaxStorefinder(number : Integer) extends ActorPropriertiesMessage
}
