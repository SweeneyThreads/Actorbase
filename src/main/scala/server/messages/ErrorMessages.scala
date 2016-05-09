package server.messages

/**
  * Created by matteobortolazzo on 02/05/2016.
  */
object ErrorMessages {
  trait ErrorMessages extends ActorbaseMessage {}
  case class InvalidQueryMessage() extends ErrorMessages
}