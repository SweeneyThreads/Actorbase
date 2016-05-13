package server.messages.query.admin

/**
  * Created by lucan on 10/05/2016.
  */
object ActorProprietiesMessages {
  trait ActorPropertiesMessage extends AdminMessage
  case class MaxRowsMessage(number : Integer) extends ActorPropertiesMessage
  case class MaxStorekeeper(number : Integer) extends ActorPropertiesMessage
  case class MaxStorefinder(number : Integer) extends ActorPropertiesMessage
}
