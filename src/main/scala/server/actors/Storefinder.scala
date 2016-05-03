package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import server.messages._
import collection.JavaConversions._

import scala.util.matching.Regex

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map */
class Storefinder extends Actor {
  var storekeepers = new ConcurrentHashMap[Regex, ActorRef]()
  storekeepers.put(".*".r, context.actorOf(Props[Storekeeper])) //Storekeeper iniziale

  // It finds the right storefinder
  def receive = {
    case m:RowMessage => {
      val storekeeper = m match {
        case InsertRowMessage(key: String, value: Array[Byte]) => findActor(key)
        case UpdateRowMessage(key: String, value: Array[Byte]) => findActor(key)
        case RemoveRowMessage(key: String) => findActor(key)
        case FindRowMessage(key: String) => findActor(key)
      }
      if(storekeeper != null)
        storekeeper ! m
      else
        println("Storefinder not found")
    }
  }

  def findActor(key:String):ActorRef = {
    for(r:Regex <- storekeepers.keys()) {
      val m = r.findFirstIn(key)
      if(m.isDefined)
        return storekeepers.get(r)
    }
    return null
  }
}
