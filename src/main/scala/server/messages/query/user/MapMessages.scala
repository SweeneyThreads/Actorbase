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

package server.messages.query.user

import server.messages.query.PermissionMessages.{ReadMessage, ReadWriteMessage}
import server.messages.query.ReplyInfo


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