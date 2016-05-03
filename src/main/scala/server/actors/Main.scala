package server.actors

import collection.JavaConversions._
import akka.actor.{Actor, Props}
import server.Server
import server.messages._

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** This actor execute client commands */
class Main extends Actor {
  var selectedDatabase = ""
  var selectedMap = ""

  // Database level commands
  def receive = {
    case d:DatabaseMessage => {
      d match {
        case ListDatabaseMessage() => {
          for(k:String <- Server.storemanagers.keys())
            println(k)
        }
        case SelectDatabaseMessage(name: String) => {
          val sm = Server.storemanagers.get(name)
          if (sm != null) {
            selectedDatabase = name
            selectedMap = ""
            println("Database " + name + " selected")
          }
          else println("Database " + name + " not found")
        }
        case CreateDatabaseMessage(name: String) => {
          val sm = Server.storemanagers.get(name)
          if (sm == null) {
            Server.storemanagers.put(name, context.actorOf(Props[Storemanager]))
            println("Database " + name + " created")
          }
          else
            println("Database " + name + " already exists")
        }
        case DeleteDatabaseMessage(name: String) => {
          val sm = Server.storemanagers.get(name)
          if (sm != null) {
            Server.storemanagers.remove(name)
            println("Database " + name + " removed")
          }
          else
            println("Database " + name + " doesn't exits")
        }
      }
    }
    case m:MapMessage => {
      if(selectedDatabase != "") {
        val sm = Server.storemanagers.get(selectedDatabase)
        if (sm != null) {
          m match {
            case SelectMapMessage(name: String) => {
              selectedMap = name
              println("Map " + name + " selected")
            }
            case _ =>
          }
          sm ! m

        }
        else
          println("Database " + selectedDatabase + " doesn't exits")
      }
      else println("Please select a database")
    }
    case r:RowMessage => {
      if(selectedDatabase != "") {
        if(selectedMap != "") {
          val sm = Server.storemanagers.get(selectedDatabase)
          if (sm != null) {
            sm ! StorefinderRowMessage(selectedMap, r)
          }
          else
            println("Database " + selectedDatabase + " doesn't exits")
        }
        else println("Please select a map")
      }
      else println("Please select a database")
    }
  }
}
