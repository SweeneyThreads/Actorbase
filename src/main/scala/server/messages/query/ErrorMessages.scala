package server.messages.query

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
object ErrorMessages {
  trait ErrorMessage {}
  case class InvalidQueryMessage() extends QueryMessage with ErrorMessage

  case class QueryErrorInfo() extends ReplyInfo
}