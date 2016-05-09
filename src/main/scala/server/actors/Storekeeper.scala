package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import server.messages._

import scala.collection.JavaConversions._

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map partition */
class Storekeeper extends Actor with akka.actor.ActorLogging  {
  var db = new ConcurrentHashMap[String, String]()

  // Row level commands
  def receive = {
    case InsertRowMessage(key: String, value: String) => {
      if(!db.containsKey(key)) {
        db.put(key, value)
        reply(key + " inserted")
        log.info(key + " inserted")
      }
      else
        reply(key + " already exist")
    }
    case UpdateRowMessage(key: String, value: String) => {
      if(!db.containsKey(key)) {
        db.put(key, value)
        reply(key + " updated")
        log.info(key + " updated")
      }
      else
        reply(key + " doesn't exist")
    }
    case RemoveRowMessage(key: String) => {
      if(db.containsKey(key)) {
        db.remove(key)
        reply(key + " removed")
        log.info(key + " removed")
      }
      else
        reply(key + " doesn't exist")
    }
    case FindRowMessage(key: String) => {
      if(db.containsKey(key)) {
        reply("The value of " + key + " is " + db.get(key))
      }
      else
        reply(key + " doesn't exist")
    }
    case ListKeysMessage() => {
      var keys =""
      for (k: String <- db.keys()) {
        keys += k + "\n"
      }
      reply(keys)
    }
  }

  private def reply(str:String, sender: ActorRef = sender): Unit = {
    Some(sender).map(_ ! str)
  }
}
