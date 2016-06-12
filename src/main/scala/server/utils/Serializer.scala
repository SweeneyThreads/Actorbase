package server.utils

import java.io._

/**
  * Created by lucan on 02/06/2016.
  */
class Serializer {

  /**
    *
    * @param obj
    * @return
    */
  def serialize(obj: Object): Array[Byte] = {
    val b: ByteArrayOutputStream = new ByteArrayOutputStream()

    val o: ObjectOutputStream = new ObjectOutputStream(b)
    o.writeObject(obj)

    b.toByteArray
  }

  /**
    *
    * @param array
    */
  def deserialize(array: Array[Byte]): Object = {
    val b: ByteArrayInputStream = new ByteArrayInputStream(array)

    val o: ObjectInputStream = new ObjectInputStream(b)
    o.readObject()

  }

}
