package server.enums

/**
  * Created by matteobortolazzo on 13/05/2016.
  * Contains reply result type classes
  */
object EnumReplyResult {
  val replyResultType = Seq(Done, Error)

  /**
    * Represents a reply result type
    */
  sealed trait ReplyResult

  /**
    * Represents a successful reply
    */
  case object Done extends ReplyResult

  /**
    * Represents an error reply
    */
  case object Error extends ReplyResult
}
