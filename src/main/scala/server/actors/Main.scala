package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, Props}
import server.EnumPermission.Permission
import server.messages._
import server.{EnumPermission, Server}

import scala.collection.JavaConversions._

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** This actor executes client commands and checks permissions */
class Main(permissions: ConcurrentHashMap[String, Permission]) extends Actor {
  var selectedDatabase = ""
  var selectedMap = ""

  def receive = {
    // Main actor responsibility
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
    // Storemanager responsibility
    case m:MapMessage => {
      if(selectedDatabase != "") {
        if (checkPermissions(m)) {
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
        else
          println("You do not have permission")
      }
      else println("Please select a database")
    }
    // Storefinder & storekeeper responsibility
    case r:RowMessage => {
      if(selectedDatabase != "") {
        if(selectedMap != "") {
          if (checkPermissions(r)) {
            val sm = Server.storemanagers.get(selectedDatabase)
            if (sm != null) {
              sm ! StorefinderRowMessage(selectedMap, r)
            }
            else
              println("Database " + selectedDatabase + " doesn't exits")
          }
          else
            println("You do not have permission")
        }
        else println("Please select a map")
      }
      else println("Please select a database")
    }
  }

  /** Checks user permissions */
  private def checkPermissions(m: ActorbaseMessage): Boolean = {
    return m match {
      case n: ReadWriteMessage => {
        val p = permissions.get(selectedDatabase)
        p != null && p == EnumPermission.ReadWrite
      }
      case n: ReadMessage => {
        val p = permissions.get(selectedDatabase)
        p != null && (p == EnumPermission.Read || p == EnumPermission.ReadWrite)
      }
      case n: NoPermissionMessage => true
    }
  }
}
