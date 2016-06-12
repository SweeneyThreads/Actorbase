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

package server.messages.query


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
