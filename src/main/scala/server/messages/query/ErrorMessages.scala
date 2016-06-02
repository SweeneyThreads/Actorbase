package server.messages.query

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/**
  * ErrorMessages are used to manage invalid operations and errors.
  */
object ErrorMessages {

  /**
    * Trait that every message which belongs to error operations has to extend.
    */
  trait ErrorMessage

  /**
    * An InvalidQueryMessage is used to inform that a requested query is not valid.
    *
    * @see QueryMessage
    * @see ErrorMessage
    */
  case class InvalidQueryMessage() extends QueryMessage with ErrorMessage

  /**
    * A QueryErrorInfo is used as response to an operation which has not been executed correctly.
    *
    * @see ReplyInfo
    */
  case class QueryErrorInfo() extends ReplyInfo
}