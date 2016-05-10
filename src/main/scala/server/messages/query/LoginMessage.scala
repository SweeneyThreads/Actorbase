package server.messages.query

/**
  * Created by matteobortolazzo on 04/05/2016.
  */
case class LoginMessage(username: String, password: String) extends QueryMessages
