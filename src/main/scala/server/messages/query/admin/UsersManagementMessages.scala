/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 SWEeneyThreads
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 *
 * @author SWEeneyThreads
 * @version 0.0.1
 * @since 0.0.1
 */

package server.messages.query.admin

import server.messages.query.ReplyInfo

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
    * not exist
    * in the 'users' map in the 'master' database.
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