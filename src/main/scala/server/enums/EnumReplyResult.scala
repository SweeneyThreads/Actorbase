package server.enums

/**
  * Created by matteobortolazzo on 13/05/2016.
  */
object EnumReplyResult {
  val replyResultType = Seq(Done, Error)

  sealed trait ReplyResult

  case object Done extends ReplyResult

  case object Error extends ReplyResult
}
