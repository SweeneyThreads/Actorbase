package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.Actor
import akka.event.Logging
import server.messages._

import scala.collection.JavaConversions._

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map partition */
class Storekeeper extends Actor {
  val log = Logging(context.system, "StorekeeperLog")
  var db = new ConcurrentHashMap[String, Array[Byte]]()

  // Row level commands
  def receive = {
    case InsertRowMessage(key: String, value: Array[Byte]) => {
      if (db.get(key) == null) {
        //introduced the test (if key already exist, print an error and does not make the insert anymore)
        db.put(key, value)
        println(key + " inserted")
      }
      else
        println(key + " already exist")
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
    case ListKeysMessage() => {
      //print all the keys on the map
      if (db.isEmpty)
        log.warning("Map is empty, please fill with some entries")
      else {
        log.info("The keys on this map are:")
        for (k: String <- db.keys()) {
          log.info(k)
        }
      }
    }
  }
}
