package server.messages.internal

/**
  * Created by matteobortolazzo on 17/05/2016.
  */
object AskMessages {
  case class AskDatabaseMessage(dbName: String)
  case class AskMapMessage(mapName:String)
}
