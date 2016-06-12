package server

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorRef

/**
  * Created by matteobortolazzo on 02/06/2016.
  * Contains the list of reference of all StoreManager actors
  */
object StaticSettings   {
  var mapManagerRefs = new ConcurrentHashMap[String, ActorRef]()
  var maxRowNumber = 256
  var ninjaNumber = 1
  var warehousemanNumber = 1
  var dataPath = "c:\\data\\actorbase_data"
}