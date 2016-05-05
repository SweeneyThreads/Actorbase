package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import server.messages._

import collection.JavaConversions._

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a database */
class Storemanager extends Actor {
  var storefinders = new ConcurrentHashMap[String, ActorRef]()
  storefinders.put("defaultMap", context.actorOf(Props[Storekeeper]))

  // Map level commands
  def receive = {
    // Replay to the main actor if there's a map with that name
    case AskMapMessage(mapName:String) => Some(sender).map(_ ! storefinders.containsKey(mapName))
    // Message that contains the real rowMessage
    case m: StorefinderRowMessage => {
      val sf = storefinders.get(m.mapName)
      if (sf != null)
        sf ! m.rowMessage
      else
        println("Map " + m.mapName + " doesn't exist")
    }
    case ListMapMessage() => {
      for (k:String <- storefinders.keys())
        println(k)
    }
    case CreateMapMessage(name: String) => {
      val sf = storefinders.get(name)
      if (sf == null) {
        storefinders.put(name, context.actorOf(Props[Storefinder]))
        println("Map " + name + " created")
      }
      else
        println("Map " + name + " already exists")
    }
    case DeleteMapMessage(name: String) => {
      val sf = storefinders.get(name)
      if (sf != null) {
        storefinders.remove(name)
        println("Map " + name + " deleted")
      }
      else
        println("Map " + name + " doesn't exits")
    }
  }
}
