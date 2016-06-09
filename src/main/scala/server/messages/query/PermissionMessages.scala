package server.messages.query

/**
  * Created by matteobortolazzo on 04/05/2016.
  */

/**
  * PermissionMessages are used to express permissions level requests.
  */
object PermissionMessages {

  /**
    * Trait that every message that defines operations that doesn't need specific permissions has to extend.
    */
  trait NoPermissionMessage

  /**
    * Trait that every message that defines an operation that needs read permissions has to extend.
    *
    * @see NoPermissionMessage
    */
  trait ReadMessage extends NoPermissionMessage

  /**
    * Trait that every message that defines an operation that needs write permissions has to extend.
    *
    * @see ReadMessage
    */
  trait ReadWriteMessage extends ReadMessage

  /**
    * Trait that every message that defines an admin operation with permissions has to extend.
    */
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
