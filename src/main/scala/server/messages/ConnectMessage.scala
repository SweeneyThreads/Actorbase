package server.messages

/**
  * Created by matteobortolazzo on 04/05/2016.
  */
case class ConnectMessage(username: String, password: String) extends ActorbaseMessage
