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
  * RowMessages are used to manage operations on items.
  */
object RowMessages {

  /**
    * Trait that every message which belongs to rows operations has to extend.
    *
    * @see UserMessage
    */
  trait RowMessage extends UserMessage

  /**
    * An InsertRowMessage is used to request the insert of an item on the selected map. An user needs Write permission
    * to request this operation, therefore this message extends ReadWriteMessage.
    * @param key The key of the item to insert
    * @param value The value of the item to insert
    *
    * @see RowMessage
    * @see ReadWriteMessage
    */
  case class InsertRowMessage(key: String, value: Array[Byte]) extends RowMessage with ReadWriteMessage

  /**
    * An UpdateRowMessage is used to request the update of an item on the selected map. An user needs Write permission
    * to request this operation, therefore this message extends ReadWriteMessage.
    * @param key The key of the item to update
    * @param value The value of the item to update
    *
    * @see RowMessage
    * @see ReadWriteMessage
    */
  case class UpdateRowMessage(key: String, value: Array[Byte]) extends RowMessage with ReadWriteMessage

  /**
    * An RemoveRowMessage is used to request the removal of an item on the selected map. An user needs Write permission
    * to request this operation, therefore this message extends ReadWriteMessage.
    * @param key The key of the item to remove
    *
    * @see RowMessage
    * @see ReadWriteMessage
    */
  case class RemoveRowMessage(key: String) extends RowMessage with ReadWriteMessage

  /**
    * An FindRowMessage is used to request the value of an item on the selected map. An user needs Write permission
    * to request this operation, therefore this message extends ReadWriteMessage.
    * @param key The key of the item to find
    *
    * @see RowMessage
    * @see ReadWriteMessage
    */
  case class FindRowMessage(key: String) extends RowMessage with ReadMessage

  /**
    * A ListKeysMessage is used to request the list of keys that compose the selected map. An user needs Read permission
    * to request this operation, therefore this message extends ReadMessage.
    *
    * @see RowMessage
    * @see ReadMessage
    */
  case class ListKeysMessage() extends RowMessage with ReadMessage

  /**
    * A StorefinderRowMessage is used to pass the RowMessage to the Storefinder which represents the selected map.
    * @param mapName The name of the selected map
    * @param rowMessage The RowMessage
    *
    * @see RowMessage
    */
  case class StorefinderRowMessage(mapName: String, rowMessage: RowMessage) extends RowMessage

  /**
    * A KeyAlreadyExistInfo is used as response to a insert item request, if the item requested for creation already
    * exist.
    *
    * @see ReplyInfo
    */
  case class KeyAlreadyExistInfo() extends ReplyInfo

  /**
    * A KeyDoesNotExistInfo is used as response to a item request, if the item requested does not exist.
    *
    * @see ReplyInfo
    */
  case class KeyDoesNotExistInfo() extends ReplyInfo

  /**
    * A ListKeyInfo is used as response to a list keys request.
    * @param keys The list of keys
    *
    * @see ReplyInfo
    */
  case class ListKeyInfo(keys: List[String]) extends ReplyInfo

  /**
    * A NoKeyInfo is used as response to a list keys request if no keys are present in the selected map.
    *
    * @see ReplyInfo
    */
  case class NoKeysInfo() extends ReplyInfo

  /**
    * A FindInfo is used as response to a find item request, returning the value of the item.
    * @param value The value of the item requested
    *
    * @see ReplyInfo
    */
  case class FindInfo(value: Array[Byte]) extends ReplyInfo
}