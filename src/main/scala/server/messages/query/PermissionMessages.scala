package server.messages.query

/**
  * Created by matteobortolazzo on 04/05/2016.
  */
object PermissionMessages {
  trait NoPermissionMessage
  trait ReadMessage extends NoPermissionMessage
  trait ReadWriteMessage extends ReadMessage
}
