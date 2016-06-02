package server

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorRef

/**
  * Created by matteobortolazzo on 02/06/2016.
  * Contains the list of reference of all StoreManager actors
  */
object StoremanagersRefs   {
  var refs = new ConcurrentHashMap[String, ActorRef]()
}