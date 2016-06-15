package server.messages.internal


object WarehousemenMessages {

  trait WarehousemanBehaviourMessage

  case object BecomeMapWarehousemanMessage extends WarehousemanBehaviourMessage

  case object EraseDatabaseMessage
}
