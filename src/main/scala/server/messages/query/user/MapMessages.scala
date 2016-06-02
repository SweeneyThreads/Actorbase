package server.messages.query.user

import server.messages.query.PermissionMessages.{ReadMessage, ReadWriteMessage}
import server.messages.query.ReplyInfo

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/**
  * MapMessages are used to manage operations on maps.
  */
object MapMessages {

  /**
    * Trait that every message which belongs to maps operations has to extend.
    *
    * @see UserMessage
    */
  trait MapMessage extends UserMessage

  /**
    * A CreateMapMessage is used to request the creation of a map with the given name. An user needs Write permission
    * to request this operation, therefore this message extends ReadWriteMessage.
    * @param name The name of the map
    *
    * @see MapMessage
    * @see ReadWriteMessage
    */
  case class CreateMapMessage(name: String) extends MapMessage with ReadWriteMessage

  /**
    * A DeleteMapMessage is used to request the deletion of the map with the given name. An user needs Write permission
    * to request this operation, therefore this message extends ReadWriteMessage.
    * @param name The name of the map
    *
    * @see MapMessage
    * @see ReadWriteMessage
    */
  case class DeleteMapMessage(name: String) extends MapMessage with ReadWriteMessage

  /**
    * A SelectMapMessage is used to request the select of the map with the given name. An user needs Read permission
    * to request this operation, therefore this message extends ReadMessage.
    * @param name The name of the map
    *
    * @see MapMessage
    * @see ReadMessage
    */
  case class SelectMapMessage(name: String) extends MapMessage with ReadMessage

  /**
    * A ListMapMessage is used to request the list of maps that compose the selected database. An user needs Read
    * permission to request this operation, therefore this message extends ReadMessage.
    *
    * @see MapMessage
    * @see ReadMessage
    */
  case class ListMapMessage() extends MapMessage with ReadMessage

  /**
    * A MapAlreadyExistInfo is used as response to a create map request, if the map requested for creation already
    * exist.
    *
    * @see ReplyInfo
    */
  case class MapAlreadyExistInfo() extends ReplyInfo

  /**
    * A MapDoesNotExistInfo is used as response to a map request, if the map requested does not exist.
    *
    * @see ReplyInfo
    */
  case class MapDoesNotExistInfo() extends ReplyInfo

  /**
    * A ListMapInfo is used as response to a list map request.
    * @param maps The list of maps
    *
    * @see ReplyInfo
    */
  case class ListMapInfo(maps: List[String]) extends ReplyInfo

  /**
    * A NoMapInfo is used as response to a list map request if no maps are present on the selected database.
    *
    * @see ReplyInfo
    */
  case class NoMapInfo() extends ReplyInfo

  /**
    * A NoMapSelectedInfo is used as response to a request on a row when no map has previously been selected.
    *
    * @see ReplyInfo
    */
  case class NoMapSelectedInfo() extends ReplyInfo
}