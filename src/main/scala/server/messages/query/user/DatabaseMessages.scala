package server.messages.query.user

import server.messages.query.PermissionMessages.{NoPermissionMessage, ReadMessage, ReadWriteMessage}
import server.messages.query.ReplyInfo

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/**
  * DatabaseMessages are used to manage operations on databases.
  */
object DatabaseMessages {

  /**
    * Trait that every message which belongs to database operations has to extend.
    *
    * @see UserMessage
    */
  trait DatabaseMessage extends UserMessage

  /**
    * A CreateDatabaseMessage is used to request the creation of a new database with the given name. An user does not
    * need a particular permission to request this operation, therefore this message extends NoPermissionMessage.
    * @param name The database name
    *
    * @see DatabaseMessage
    * @see NoPermissionMessage
    */
  case class CreateDatabaseMessage(name: String) extends DatabaseMessage with NoPermissionMessage

  /**
    * A DeleteDatabaseMessage is used to request the deletion of the database with the given name. An user needs Write
    * permission to request this operation, therefore this message extends ReadWriteMessage.
    * @param name The database name
    *
    * @see DatabaseMessage
    * @see ReadWriteMessage
    */
  case class DeleteDatabaseMessage(name: String) extends DatabaseMessage with ReadWriteMessage

  /**
    * A SelectDatabaseMessage is used to request the select of the database with the given name. An user needs Read
    * permission to request this operation, therefore this message extends ReadMessage.
    * @param name The database name
    *
    * @see DatabaseMessage
    * @see ReadMessage
    */
  case class SelectDatabaseMessage(name: String) extends DatabaseMessage with ReadMessage

  /**
    * A ListDatabaseMessage is used to request the list of databases present on the server. An user needs Read
    * permission to request this operation, therefore this message extends ReadMessage.
    *
    * @see DatabaseMessage
    * @see ReadMessage
    */
  case class ListDatabaseMessage() extends DatabaseMessage with ReadMessage

  /**
    * A DBAlreadyExistInfo is used as response to a create database request, if the database requested for creation
    * already exists
    *
    * @see ReplyInfo
    */
  case class DBAlreadyExistInfo() extends ReplyInfo

  /**
    * A DBDoesNotExistInfo is used as response to a database request which asks for a database that does not exist.
    *
    * @see ReplyInfo
    */
  case class DBDoesNotExistInfo() extends ReplyInfo

  /**
    * A ListDBInfo is used as response to a list database request.
    * @param dbs The list of databases.
    *
    * @see ReplyInfo
    */
  case class ListDBInfo(dbs: List[String]) extends ReplyInfo

  /**
    * A NoDBInfo is used as response to a list database request if no databases are present on the server.
    *
    * @see ReplyInfo
    */
  case class NoDBInfo() extends ReplyInfo

  /**
    * A NoDBSelectedInfo is used as response to a request on a map or on a row when no database has previously been
    * selected.
    *
    * @see ReplyInfo
    */
  case class NoDBSelectedInfo() extends ReplyInfo
}