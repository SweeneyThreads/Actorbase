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

package server.utils.fileManagerLibrary

import java.io._
import java.util
import java.util.concurrent.ConcurrentHashMap

import server.StaticSettings
import server.utils.FileManager


/**
  * A SingleFileManager saves for each map a file of maps and a file of values.
  *
  * @param dbName the database Name.
  * @param mapName the map Name.
  *
  */
class SingleFileManager (dbName: String, mapName: String) extends FileManager {

  //checking folder tree
  val dbFolder = new File(s"${StaticSettings.dataPath}\\$dbName")
  if (!dbFolder.exists) dbFolder.mkdir
  val mapFolder = new File(s"${StaticSettings.dataPath}\\$dbName\\$mapName")
  if (!mapFolder.exists) mapFolder.mkdir



  /**
    * The absolute path of the keyFile.
    */
  val keyFilePath : String = s"${StaticSettings.dataPath}\\$dbName\\$mapName\\keyFile.acb"

  //Create one empty map if not already exists
  val keyFile = new File(keyFilePath)
  if (!keyFile.exists) {
    keyFile.createNewFile
    val map=new ConcurrentHashMap[String,Bounds]()
    writeMap(map)
  }

  /**
    * The absolute path of the valueFile.
    */
  val valuesPath : String = s"${StaticSettings.dataPath}\\$dbName\\$mapName\\valueFile.acb"


  /**
    * sets the strategy for deleting one entry in the file that stores the values
    */
  var removeStrategy : RemoveStrategy = new SpoolerRemove

  /**
    * the file that stores the values
    */
  var valuesFile = new RandomAccessFile(valuesPath,"rw")


  /**
    * Inserts an entry in the files
    *
    * @param key   The key of the entry.
    * @param value The value of the entry
    */
  override def InsertEntry(key: String, value: Array[Byte]): Unit = {
    val init = insertValue(valuesFile,value)
    insertKey(key,init,value.length)
  }

  /**
    * Updates the value of the entry with the given key in the file.
    *
    * @param key   The key of the entry.
    * @param value The value of the entry
    */
  override def UpdateEntry(key: String, value: Array[Byte]): Unit = {
    RemoveEntry(key)
    InsertEntry(key,value)
  }

  /**
    * Removes the entry with the given key from the file.
    *
    * @param key The key of the entry.
    */
  override def RemoveEntry(key: String): Unit = {
    val keyMap=readMap()
    val bounds = keyMap get key
    removeValue(valuesFile,bounds.init,bounds.offset)
    removeKey(keyMap,key)
    writeMap(keyMap)
  }

  /**
    * Reads an entire map.
    *
    * @return The map to read.
    */
  override def ReadMap(): ConcurrentHashMap[String, Array[Byte]] = {
    val aux = new ConcurrentHashMap[String, Array[Byte]]
    val keyMap=readMap()
    val k = keyMap.keySet.iterator()
    while (k.hasNext) {
      val e = k.next()
      aux.put(e,findValue(valuesFile,keyMap.get(e).init,keyMap.get(e).offset))
    }
    aux
  }

  /**
    * Writes an entire map in the file.
    *
    * @param map The map to write.
    */
  override def WriteMap(map: util.HashMap[String, Array[Byte]]): Unit = ???


  /**
    * Inserts a key in the "key File".
    *
    * @param key  The name of the key.
    * @param from The pointer of the Byte stream at the beginning of the value.
    * @param off  The length of the value.
    */
  private def insertKey(key: String, from: Long, off: Long): Unit = {
    val keyMap = readMap()
    keyMap.put(key, new Bounds(from,off))
    writeMap(keyMap)
  }

  /**
    * Removes a key from the "key file" and changes the offsets of the other keys, accordingly to the changes in the
    * "value file" that @see removeValue could have done.
    *
    * @param keyMap The map containing the keys.
    * @param key    The key to remove.
    */
  private def removeKey(keyMap: ConcurrentHashMap[String,Bounds], key: String): Unit ={
    val init=keyMap.get(key).init
    val off=keyMap.get(key).offset
    keyMap.remove(key)


    val k = keyMap.keySet.iterator()
    while (k.hasNext) {
      val e = k.next()
      if (keyMap.get(e).init > init)
        keyMap.replace(e,new Bounds(keyMap.get(e).init-off,keyMap.get(e).offset))
    }
  }

  /**
    * Appends a value at the bottom of the "value file".
    *
    * @param file   The "value file".
    * @param value  The value to insert as Byte array.
    * @return       The pointer at the beginning of the inserted file.
    */
  private def insertValue(file: RandomAccessFile, value: Array[Byte]): Long = {
    val init=file.length
    file.seek(init)
    file.write(value)
    init
  }

  /**
    * Remove 'off' Byte from 'init' on, from the "value file" accordingly with the remove strategy specified in
 *
    * @see removeStrategy param of this class.
    * @param file The "value file".
    * @param init The pointer of the Byte stream at the beginning of the value.
    * @param off  The size of the value.
    */
  private def removeValue(file: RandomAccessFile, init: Long, off: Long): Unit = {
    removeStrategy.remove(file, init, off)
  }

  /**
    * Finds the Bytes in the "value file" between 'init' and 'off'
    *
    * @param file The "value file".
    * @param init The pointer of the Byte stream at the beginning of the value.
    * @param off  The size of the value.
    * @return     The value in byte of the searched value.
    */
  private def findValue(file: RandomAccessFile, init: Long, off:Long) : Array[Byte] = {
    file.seek(init)
    val aux = new Array[Byte](off.toInt)
    file.read(aux,0,off.toInt)
    aux
  }


  /**
    * Deserializes the CuncurrentHashMap containing the keys.
    *
    * @return the map of the keys.
    */
  private def readMap(): ConcurrentHashMap[String,Bounds] = {
    val istream = new ObjectInputStream(new FileInputStream(keyFilePath))
    val keyMap = istream.readObject().asInstanceOf[ConcurrentHashMap[String,Bounds]]
    istream.close()
    keyMap
  }

  /**
    * Serializes the CuncurrentHashMap containing the keys.
    *
    * @param map  Map of they keys to serialize.
    */
  private def writeMap(map: ConcurrentHashMap[String,Bounds]) : Unit = {
    val ostream = new ObjectOutputStream(new FileOutputStream(keyFilePath))
    ostream.writeObject(map)
    ostream.close()
  }


}

/**
  * Bounds of a value in the byte array.
  *
  * @param init     The pointer at the beginning of the value.
  * @param offset   The size of the value.
  */
case class Bounds(var init: Long, var offset: Long)
