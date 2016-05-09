package server.actors

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import server.messages._

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}
import scala.util.matching.Regex

/**
  * Created by matteobortolazzo on 02/05/2016.
  */

/** This actor represent a map */
class Storefinder extends Actor with akka.actor.ActorLogging {

  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  var storekeepers = new ConcurrentHashMap[Regex, ActorRef]()
  storekeepers.put(".*".r, context.actorOf(Props[Storekeeper])) // Startup storekeeper

  def receive = {
    case m:RowMessage => {
      m match {
        case ListKeysMessage() => {
          val origSender = sender
          val storeKeeperNumber = storekeepers.keys().length
          var messagesReceived = 0
          var keys = ""
          for (r: Regex <- storekeepers.keys()) {
            val future = storekeepers.get(r) ? m
            future.onComplete {
              case Success(result) => {
                messagesReceived = messagesReceived + 1
                keys += result
                if(messagesReceived == storeKeeperNumber) {
                  if(keys == "") reply("No keys in this map")
                  else reply(result.toString, origSender)
                }
              }
              case Failure(t) => {
                messagesReceived = messagesReceived + 1
                log.error("Error sending message: " + t.getMessage)
              }
            }
          }
        }
        case _ => {
          val storekeeper = m match {
            case InsertRowMessage(key: String, value:String) => findActor(key)
            case UpdateRowMessage(key: String, value:String) => findActor(key)
            case RemoveRowMessage(key: String) => findActor(key)
            case FindRowMessage(key: String) => findActor(key)
          }
          if (storekeeper != null){
            val origSender = sender
            val future = storekeeper ? m
            future.onComplete {
              case Success(result) => reply(result.toString, origSender)
              case Failure(t) => log.error("Error sending message: " + t.getMessage)
            }
          }
          else
            reply("Storefinder not found")
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

  private def reply(str:String, sender: ActorRef = sender): Unit = {
    Some(sender).map(_ ! str)
  }
}
