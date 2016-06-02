package server.messages.query

/**
  * Created by matteobortolazzo on 04/05/2016.
  */

/**
  * A LoginMessage is used to request a login operation.
  * @param username The username of the user who wants to login
  * @param password The password of the user who wants to login
  *
  * @see QueryMessage
  */
case class LoginMessage(username: String, password: String) extends QueryMessage
