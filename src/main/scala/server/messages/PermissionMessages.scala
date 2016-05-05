package server.messages

/**
  * Created by matteobortolazzo on 04/05/2016.
  */

trait NoPermissionMessage

trait ReadMessage extends NoPermissionMessage

trait ReadWriteMessage extends ReadMessage
