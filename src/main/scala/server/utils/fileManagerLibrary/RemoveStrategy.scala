package server.utils.fileManagerLibrary

import java.io.RandomAccessFile

/**
  * Created by kurt on 06/06/2016.
  */
trait RemoveStrategy {
  /**
    * Remove the selected section from a file.
    *
    * @param file The file.
    * @param init The beginning of the section.
    * @param off  The length of the section.
    */
  def remove(file: RandomAccessFile, init: Long, off: Long): Unit
}
