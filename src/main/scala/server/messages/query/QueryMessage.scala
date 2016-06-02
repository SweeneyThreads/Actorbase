package server.messages.query

/**
  * Created by lucan on 10/05/2016.
  */

/**
  * Trait that every message that belongs to query operations has to extend.
  */
trait QueryMessage

case class ServiceErrorInfo(error : String) extends ReplyInfo
