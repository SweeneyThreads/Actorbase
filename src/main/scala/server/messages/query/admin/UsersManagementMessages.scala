package server.messages.query.admin

import server.messages.query.ReplyInfo

/**
  * Created by lucan on 10/05/2016.
  */

/**
  * UserManagementMessages are used to manage the users who have access to the server.
  */
object UsersManagementMessages {

  /**
    * Trait that every message that belongs to user management operations has to extend.
    *
    * @see AdminMessage
    */
  trait UsersManagementMessage extends AdminMessage

  /**
    * A ListUserMessage is used to ask for the list of users who have access to the server.
    *
    * @see UsersManagementMessage
    */
  case class ListUserMessage() extends UsersManagementMessage

  /**
    * An AddUserMessage is used to add an user with username and password to the list of users who have access to
    * the server
 *
    * @param username The user's username
    * @param password The user's password
    * @see UsersManagementMessage
    */
  case class AddUserMessage(username : String, password :String) extends UsersManagementMessage

  /**
    * A RemoveUserMessage is used to remove an user from the list of users who have acces to the server.
 *
    * @param username The user's username
    * @see UsersManagementMessages
    */
  case class RemoveUserMessage(username : String) extends UsersManagementMessage

  case class ListUserInfo(userList : List[String]) extends ReplyInfo
  case class NoUserInfo() extends ReplyInfo

}