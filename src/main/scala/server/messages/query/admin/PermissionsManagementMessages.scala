package server.messages.query.admin
import java.util

import server.enums.EnumPermission.UserPermission
import server.messages.query.ReplyInfo
/**
  * Created by lucan on 10/05/2016.
  */

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
