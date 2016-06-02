package server.messages.query

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/**
  * ErrorMessages are used to manage invalid operations and errors.
  */
object ErrorMessages {

  /**
    * This trait has to be extended by messages which have to express an error condition.
    */
  trait ErrorMessage {}
  case class InvalidQueryMessage() extends QueryMessage with ErrorMessage

  case class QueryErrorInfo() extends ReplyInfo
}