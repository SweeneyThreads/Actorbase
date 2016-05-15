package server.messages.query.admin

/**
  * Created by lucan on 10/05/2016.
  */
object ActorPropetiesMessages {
  trait ActorPropertiesMessage extends AdminMessage
  case class SetNinjaMessage(number : Integer) extends ActorPropertiesMessage
  case class SetWarehousemanMessage(number : Integer) extends ActorPropertiesMessage
  case class MaxRowsMessage(number : Integer) extends ActorPropertiesMessage
  case class MaxStorekeeperMessage(number : Integer) extends ActorPropertiesMessage
  case class MaxStorefinderMessage(number : Integer) extends ActorPropertiesMessage
}
