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

package server.messages.internal

import java.util.concurrent.ConcurrentHashMap

import server.messages.query.ReplyInfo

import scala.collection.immutable.HashMap


/**
  * StoreMessages are used to express operations about persistence of data.
  */
object StorageMessages {

  /**
    * Trait that every message that belongs to storage operations has to extend.
    */
  trait StorageMessage

  /**
    * A WriteMapMessage is used to write data to disk during shut down operation.
    *
    * @param map The map of data that has to be written
    * @see StorageMessage
    */
  case class WriteMapMessage (map: HashMap[String, Array[Byte]]) extends StorageMessage

  /**
    * A ReadMapMessage is used to read data during the start up operation.
    *
    * @see StorageMessage
    */
  case class ReadMapMessage () extends StorageMessage


  case class ReadMapReply(val map: ConcurrentHashMap[String,Array[Byte]]) extends StorageMessage
}
