package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.Actor
import server.messages.{RemoveRowMessage, FindRowMessage, InsertRowMessage, UpdateRowMessage}

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map partition */
class Storekeeper extends Actor {
  var db = new ConcurrentHashMap[String, Array[Byte]]()

  // Row level commands
  def receive = {
    case InsertRowMessage(key: String, value: Array[Byte]) => {
      db.put(key, value)
      println(key + " inserted")
    }
    case UpdateRowMessage(key: String, value: Array[Byte]) => {
      if(db.get(key) != null) {
        db.put(key, value)
        println(key + " updated")
      }
      else
        println(key + " doesn't exist")
    }
    case RemoveRowMessage(key: String) => {
      val value = db.remove(key)
      if(value != null)
        println(key + " removed")
      else
        println(key + " doesn't exist")
    }
    case FindRowMessage(key: String) => {
      val value = db.get(key)
      if(value != null)
        println("The value of " + key + " is " + value.toString())
      else
        println(key + " doesn't exist")
    }
  }
}
