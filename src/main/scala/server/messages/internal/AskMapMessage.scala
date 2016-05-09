package server.messages.internal

import server.messages.ActorbaseMessage

/**
  * Created by matteobortolazzo on 05/05/2016.
  */
case class AskMapMessage(mapName:String) extends ActorbaseMessage