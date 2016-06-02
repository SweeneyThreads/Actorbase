package server.messages.internal

/**
  * Created by matteobortolazzo on 17/05/2016.
  */

/**
  * AskMessages are used to check if an element (a database, a map, ...) is present.
  */
object AskMessages {

  /**
    * An AskDatabaseMessage is used to check the presence of a database by his name.
    * @param dbName The database name.
    */
  case class AskDatabaseMessage(dbName: String)

  /**
    * An AskMapMessage is used to check the presence of a map by his name.
    * @param mapName The map name.
    */
  case class AskMapMessage(mapName:String)
}
