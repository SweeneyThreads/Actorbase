package server.messages.query

/**
  * Created by matteobortolazzo on 04/05/2016.
  */
object PermissionMessages {
  trait NoPermissionMessage
  trait ReadMessage extends NoPermissionMessage
  trait ReadWriteMessage extends ReadMessage
  trait AdminPermissionMessage

  /**
    * A NoReadPermissionInfo is a ReplyInfo containing the response to print on the console when a user tries to
    * query a database on which he/she has no Read permission.
    */
  case class NoReadPermissionInfo() extends ReplyInfo

  /**
    * A NoWritePermissionInfo is a ReplyInfo containing the response to print on the console when a user tries to
    * modify something in a database on which he/she has no ReadWrite permission.
    */
  case class NoWritePermissionInfo() extends ReplyInfo

  /**
    * A NoAdminPermissionInfo is a ReplyInfo containing the response to print on the console when a user tries to
    * use an admin command without being an admin.
    */
  case class NoAdminPermissionInfo() extends ReplyInfo

}
