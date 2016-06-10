package server.utils.fileManagerLibrary

import java.io.RandomAccessFile

/**
  * Created by kurt on 06/06/2016.
  */
class SpoolerRemove extends RemoveStrategy {

  /**
    * Remove the selected section from a file shifting back 8kB at a time.
    *
    * @param file The file.
    * @param init The beginning of the section.
    * @param off  The length of the section.
    */
  override def remove(file: RandomAccessFile, init: Long, off: Long): Unit = {
    var remain: Long = file.length-(init+off)
    val byteLength = 8192
    var aux = init
    while(remain > byteLength){
      val ba = new Array[Byte](byteLength)
      file.seek(aux+off)
      file.read(ba,0,byteLength)
      file.seek(aux)
      file.write(ba)
      aux = aux+ byteLength
      remain = remain - byteLength
    }
    if(remain != 0){
      val ba = new Array[Byte](remain.toInt)
      file.seek(aux+off)
      file.read(ba,0,remain.toInt)
      file.seek(aux)
      file.write(ba)
      aux = aux+ remain.toInt
      val f = file.getChannel
      f.truncate(aux)
    }
  }
}