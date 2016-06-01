package server.utils

import java.util

/**
  * Created by matteobortolazzo on 09/05/2016.
  *
  * Writes and reads from the filesystem.
  *
  * @param path The folder path.
  */
class FileManager(path: String) {

  /**
    * Inserts an entry in the files
    *
    * @param key The key of the entry.
    * @param value The value of the entry
    */
  def InsertEntry(key: String, value: Array[Byte]): Unit = {
    //TODO
  }

  /**
    * Updates the value of the entry with the given key in the file.
    *
    * @param key The key of the entry.
    * @param value The value of the entry
    */
  def UpdateEntry(key:String, value: Array[Byte]): Unit ={
    //TODO
  }

  /**
    * Removes the entry with the given key from the file.
    *
    * @param key The key of the entry.
    */
  def RemoveEntry(key: String) : Unit= {
    //TODO
  }

  /**
    * Writes an entire map in the file.
    *
    * @param map The map to write.
    */
  def WriteMap(map: util.HashMap[String, Array[Byte]]): Unit ={
    //TODO
  }

  /**
    * Reads an entire map.
    *
    * @return The map to read.
    */
  def ReadMap() :  util.HashMap[String, Array[Byte]] ={
    //TODO
    new util.HashMap[String, Array[Byte]]()
  }
}