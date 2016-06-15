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

package server.utils

import java.util
import java.util.concurrent.ConcurrentHashMap

import server.utils.fileManagerLibrary.RemoveStrategy

/**
  * Writes and reads from the filesystem.
  *
  */
trait FileManager {
  var removeStrategy : RemoveStrategy

  /**
    * Inserts an entry in the files
    *
    * @param key The key of the entry.
    * @param value The value of the entry
    */
  def InsertEntry(key: String, value: Array[Byte])

  /**
    * Updates the value of the entry with the given key in the file.
    *
    * @param key The key of the entry.
    * @param value The value of the entry
    */
  def UpdateEntry(key:String, value: Array[Byte])

  /**
    * Removes the entry with the given key from the file.
    *
    * @param key The key of the entry.
    */
  def RemoveEntry(key: String)

  /**
    * Writes an entire map in the file.
    *
    * @param map The map to write.
    */
  def WriteMap(map: util.HashMap[String, Array[Byte]])

  /**
    * Reads an entire map.
    *
    * @return The map to read.
    */
  def ReadMap() :  ConcurrentHashMap[String, Array[Byte]]

  /**
    * Erases the whole map.
    */
  def EraseMap() : Unit


}