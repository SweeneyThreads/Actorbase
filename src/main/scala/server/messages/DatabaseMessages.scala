package server.messages

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
trait DatabaseMessage extends ActorbaseMessage

case class CreateDatabaseMessage(name: String) extends DatabaseMessage with NoPermissionMessage

case class DeleteDatabaseMessage(name: String) extends DatabaseMessage with ReadWriteMessage

case class SelectDatabaseMessage(name: String) extends DatabaseMessage with ReadMessage

case class ListDatabaseMessage() extends DatabaseMessage with ReadMessage