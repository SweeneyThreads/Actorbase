package server.messages.query.admin
import server.EnumPermission.Permission
/**
  * Created by lucan on 10/05/2016.
  */
object PermissionsManagementMessages {
  trait PermissionsManagementMessage extends AdminMessage
  case class AddPermissionMessage(username : String, database: String, permissionType : Permission) extends PermissionsManagementMessage
  case class RemovePermissionMessage(username : String, database: String) extends PermissionsManagementMessage
}
