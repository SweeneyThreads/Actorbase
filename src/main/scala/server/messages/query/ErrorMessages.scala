package server.messages.query

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
object ErrorMessages {
  trait ErrorMessages extends QueryMessages {}
  case class InvalidQueryMessage() extends ErrorMessages
}