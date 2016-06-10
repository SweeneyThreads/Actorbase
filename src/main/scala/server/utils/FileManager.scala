package server.utils

import java.util
import java.util.concurrent.ConcurrentHashMap

import server.utils.fileManagerLibrary.RemoveStrategy

/**
  * Created by matteobortolazzo on 09/05/2016.
  *
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
}