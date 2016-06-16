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

import java.io.RandomAccessFile

/**
  * It's an implementation of the Remove strategy.
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