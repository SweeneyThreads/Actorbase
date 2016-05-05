package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, Props}
import server.EnumPermission.Permission
import server.messages._
import server.{EnumPermission, Server}

import scala.collection.JavaConversions._
import scala.concurrent.Future

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** This actor executes client commands and checks permissions */
class Main(permissions: ConcurrentHashMap[String, Permission]) extends Actor {

  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._

  implicit val ec = global

  var selectedDatabase = ""
  var selectedMap = ""

  def receive = {
    // Main actor responsibility
    case d:DatabaseMessage => manageDatabaseLevelMessage(d)
    // Not a DatabaseMessage
    case m:ActorbaseMessage => manageNotDatabaseLevelMessage(m)
  }

  private def manageDatabaseLevelMessage(message: DatabaseMessage): Unit = {
    message match {
      case ListDatabaseMessage() => {
        for (k: String <- Server.storemanagers.keys()) {
          if (permissions.get(k) != null)
            println(k)
        }
      }
      case SelectDatabaseMessage(name: String) => {
        if (Server.storemanagers.containsKey(name) && checkPermissions(message, name)) {
          selectedDatabase = name
          selectedMap = ""
          println("Database " + name + " selected")
        }
        else println("Invalid database")
      }
      case CreateDatabaseMessage(name: String) => {
        if (Server.storemanagers.containsKey(name) && checkPermissions(message, name)) {
          Server.storemanagers.put(name, context.actorOf(Props[Storemanager]))
          println("Database " + name + " created")
        }
        else println("Invalid database")
      }
      case DeleteDatabaseMessage(name: String) => {
        if (Server.storemanagers.containsKey(name) && checkPermissions(message, name)) {
          Server.storemanagers.remove(name)
          println("Database " + name + " removed")
        }
        else println("Invalid database")
      }
    }
  }

  private def manageNotDatabaseLevelMessage(message: ActorbaseMessage): Unit ={
    if(selectedDatabase != "") {
      message match {
        // Storemanager responsibility
        case m: MapMessage => {
          if (Server.storemanagers.containsKey(selectedDatabase) && checkPermissions(m, selectedDatabase)) {
            val sm = Server.storemanagers.get(selectedDatabase)
            m match {
              case SelectMapMessage(name: String) => {
                // Ask the storeManager if there's a map with that name
                implicit val timeout = Timeout(25 seconds)
                val future = sm ? new AskMapMessage(name)
                future.map { result =>
                  // If there's a map with that name
                  if(result.asInstanceOf[Boolean]) {
                    selectedMap = name
                    println("Map " + name + " selected")
                  }
                  else println("Invalid map")
                }
              }
              case _ => sm ! m
            }
          }
          else println("You not allow to run this command")
        }
        // Storefinder & storekeeper responsibility
        case r: RowMessage => {
          if (selectedMap != "") {
            if(Server.storemanagers.containsKey(selectedDatabase) && checkPermissions(r, selectedDatabase)) {
              val sm = Server.storemanagers.get(selectedDatabase)
              sm ! StorefinderRowMessage(selectedMap, r)
            }
            else println("You not allow to run this command")
          }
          else println("Please select a map")
        }
      }
      if (checkPermissions(message, selectedDatabase)) {

      }
      else println("You do not have permission")
    }
  }

  /** Checks user permissions */
  private def checkPermissions(message: ActorbaseMessage, dbName:String): Boolean = {
    return message match {
      case n: ReadWriteMessage => {
        val p = permissions.get(dbName)
        p != null && p == EnumPermission.ReadWrite
      }
      case n: ReadMessage => {
        val p = permissions.get(dbName)
        p != null && (p == EnumPermission.Read || p == EnumPermission.ReadWrite)
      }
      case n: NoPermissionMessage => true
    }
  }
}