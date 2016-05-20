package server.messages.query.admin

/**
  * Created by lucan on 10/05/2016.
  */
object UsersManagementMessages {
  trait UsersManagementMessage extends AdminMessage
  case class ListUserMessage() extends UsersManagementMessage
  case class AddUserMessage(username : String, password :String) extends UsersManagementMessage
  case class RemoveUserMessage(username : String) extends UsersManagementMessage
}