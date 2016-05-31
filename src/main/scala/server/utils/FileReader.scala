package server.utils

import java.io.{FileNotFoundException, IOException}
import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.event.LoggingAdapter
import org.json.JSONObject
import server.enums.EnumPermission
import server.enums.EnumPermission.UserPermission

/**
  * Created by matteobortolazzo on 09/05/2016.
  */

class FileReader(fileName: String, log:LoggingAdapter) {
  def WriteEntry(key: String, value: Array[Byte]): Unit = {
    //TODO
  }
  def RemoveEntry(key: String) : Unit= {
    //TODO
  }
  def UpdateEntry(key:String): Unit ={
    //TODO
  }
  def WriteMap(map: util.HashMap[String, Array[Byte]]): Unit ={
    //TODO
  }
  def ReadMap() :  util.HashMap[String, Array[Byte]] ={
    //TODO
    new util.HashMap[String, Array[Byte]]()
  }
}