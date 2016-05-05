package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import server.messages._

import scala.collection.JavaConversions._
import scala.util.matching.Regex

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map */
class Storefinder extends Actor {
  val log = Logging(context.system, this)
  var storekeepers = new ConcurrentHashMap[Regex, ActorRef]()
  storekeepers.put(".*".r, context.actorOf(Props[Storekeeper])) // Startup storekeeper

  def receive = {
    case m:RowMessage => {
      m match {
        case ListKeysMessage() => {
          for (r: Regex <- storekeepers.keys()) {
            storekeepers.get(r) ! m
          }
        }
        case _ => {
          val storekeeper = m match {
            case InsertRowMessage(key: String, value: Array[Byte ]) => findActor(key)
            case UpdateRowMessage(key: String, value: Array[Byte]) => findActor(key)
            case RemoveRowMessage(key: String) => findActor(key)
            case FindRowMessage(key: String) => findActor(key)
          }
          if (storekeeper != null)
            storekeeper ! m
          else
            println("Storefinder not found")
        }
      }
    }
  }

  //** Finds the storekeeper that could contain the key */
  def findActor(key:String):ActorRef = {
    for(r:Regex <- storekeepers.keys()) {
      val m = r.findFirstIn(key)
      if(m.isDefined)
        return storekeepers.get(r)
    }
    return null
  }
}
