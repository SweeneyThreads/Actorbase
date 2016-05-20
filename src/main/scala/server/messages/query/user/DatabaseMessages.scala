package server.messages.query.user

import server.messages.query.PermissionMessages.{NoPermissionMessage, ReadMessage, ReadWriteMessage}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
object DatabaseMessages {
  trait DatabaseMessage extends UserMessage
  case class CreateDatabaseMessage(name: String) extends DatabaseMessage with NoPermissionMessage
  case class DeleteDatabaseMessage(name: String) extends DatabaseMessage with ReadWriteMessage
  case class SelectDatabaseMessage(name: String) extends DatabaseMessage with ReadMessage
  case class ListDatabaseMessage() extends DatabaseMessage with ReadMessage
}