package server.messages.internal

import java.util.concurrent.ConcurrentHashMap

import server.messages.query.ReplyInfo

import scala.collection.immutable.HashMap

/**
  * Created by eliamaino on 02/06/16.
  */

/**
  * StoreMessages are used to express operations about persistence of data.
  */
object StorageMessages {

  /**
    * Trait that every message that belongs to storage operations has to extend.
    */
  trait StorageMessage

  /**
    * A WriteMapMessage is used to write data to disk during shut down operation.
    *
    * @param map The map of data that has to be written
    * @see StorageMessage
    */
  case class WriteMapMessage (map: HashMap[String, Array[Byte]]) extends StorageMessage

  /**
    * A ReadMapMessage is used to read data during the start up operation.
    *
    * @see StorageMessage
    */
  case class ReadMapMessage () extends StorageMessage


  case class ReadMapReply(val map: ConcurrentHashMap[String,Array[Byte]]) extends StorageMessage
}
