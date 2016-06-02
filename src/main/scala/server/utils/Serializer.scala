package server.utils

import java.io._

/**
  * Created by lucan on 02/06/2016.
  */
class Serializer {

  def serialize(obj: Unit): Array[Byte] = {
    try {
      val b: ByteArrayOutputStream = new ByteArrayOutputStream()
      try {
        val o: ObjectOutputStream = new ObjectOutputStream(b)
        o.writeObject(obj)
      }
      return b.toByteArray()
    }
  }

  def deserialize(array: Array[Byte]): Unit = {
    try {
      val b: ByteArrayInputStream = new ByteArrayInputStream(array)
      try {
        val o: ObjectInputStream = new ObjectInputStream(b)
        return o.readObject();
      }
    }
  }

}
