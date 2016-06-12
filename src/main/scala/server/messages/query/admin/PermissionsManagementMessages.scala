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

import java.util

import server.enums.EnumPermission.UserPermission
import server.messages.query.ReplyInfo

/**
  * PermissionManagementMessages are used to manage operations about user's permissions to access data.
  */
object PermissionsManagementMessages {

  /**
    * Trait that every message that belongs to permissions management operations has to extend.
    *
    * @see AdminMessage
    */
  trait PermissionsManagementMessage extends AdminMessage

  /**
    * A ListPermissionMessage is used to request the list of permission related to a specific user.
    * @param username The user's username.
    *
    * @see PermissionManagementMessage
    */
  case class ListPermissionMessage(username : String) extends PermissionsManagementMessage

  /**
    * An AddPermissionMessage is used to request the addition of a permission to a database for an user.
    * @param username The user's username.
    * @param database The database to add permission to.
    * @param permissionType The type of permission to add.
    *
    * @see UserPermission
    * @see PermissionManagementMessage
    */
  case class AddPermissionMessage(username : String, database: String, permissionType : UserPermission) extends PermissionsManagementMessage

  /**
    * A RemovePermissionMessage is used to request the removal of a permission to a database for an user.
    * @param username The user's username
    * @param database The database to remove permission from.
    *
    * @see PermissionManagementMessage
    */
  case class RemovePermissionMessage(username : String, database: String) extends PermissionsManagementMessage

  /**
    * A ListPermissionsInfo is used to store the list of permissions assigned to a user
    * @param permissions contains the map with the user permissions
    *
    * @see ReplyInfo
    */
  case class ListPermissionsInfo(permissions: util.HashMap[String, UserPermission]) extends ReplyInfo

}
