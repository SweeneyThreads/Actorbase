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

  /**
    * A ListUserInfo is used as a ReplyInfo to format the response from a ListUserMessage. The ReplyBuilder
    * print on the console the list wrapped in this ReplyInfo class.
    *
    * @param userList the List containing the usernames of the users
    */
  case class ListUserInfo(userList : List[String]) extends ReplyInfo

  /**
    * A NoUserInfo is user as a ReplyInfo to handle a request that need to modify something of a user that does
    * not exist in the 'users' map in the 'master' database.
    */
  case class NoUserInfo() extends ReplyInfo

  /**
    * An AddUserInfo is a ReplyInfo containing the response to print on the console when an AddUserMessage has
    * been processed.
    */
  case class AddUserInfo() extends ReplyInfo

  /**
    * A RemoveUserInfo is a ReplyInfo containing the response to print on the console when a RemoveUserMessage has
    * been processed.
    */
  case class RemoveUserInfo() extends ReplyInfo
}