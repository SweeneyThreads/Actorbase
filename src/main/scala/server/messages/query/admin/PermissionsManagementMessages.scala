package server.messages.query.admin
import server.enums.EnumPermission
import server.enums.EnumPermission.UserPermission
/**
  * Created by lucan on 10/05/2016.
  */
object PermissionsManagementMessages {
  trait PermissionsManagementMessage extends AdminMessage
  case class ListPermissionMessage(username : String) extends PermissionsManagementMessage
  case class AddPermissionMessage(username : String, database: String, permissionType : UserPermission) extends PermissionsManagementMessage
  case class RemovePermissionMessage(username : String, database: String) extends PermissionsManagementMessage
}
