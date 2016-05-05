package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
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
    case m: DatabaseMessage => manageDatabaseMessage(m)
    case m: ActorbaseMessage => manageNotDatabaseMessage(m)
  }

  /** Manages database messages */
  private def manageDatabaseMessage(message: DatabaseMessage): Unit = {
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
        else println("Invalid operation")
      }
      case CreateDatabaseMessage(name: String) => {
        if (Server.storemanagers.containsKey(name) && checkPermissions(message, name)) {
          Server.storemanagers.put(name, context.actorOf(Props[Storemanager]))
          println("Database " + name + " created")
        }
        else println("Invalid operation")
      }
      case DeleteDatabaseMessage(name: String) => {
        if (Server.storemanagers.containsKey(name) && checkPermissions(message, name)) {
          Server.storemanagers.remove(name)
          println("Database " + name + " removed")
        }
        else println("Invalid operation")
      }
    }
  }

  /** Manages map or row messages */
  private def manageNotDatabaseMessage(message: ActorbaseMessage): Unit = {
    if (selectedDatabase != "") {
      // If the database exists and the user has the permissions for the operation
      if (Server.storemanagers.containsKey(selectedDatabase) && checkPermissions(message, selectedDatabase)) {
        // Gets the right storemanager
        val storemanager = Server.storemanagers.get(selectedDatabase)
        message match {
          case m: MapMessage => manageMapMessage(m, storemanager)
          case m: RowMessage => manageRowMessage(m, storemanager)
        }
      }
      else println("Invalid operation")
    }
    else println("Please select a database")
  }

  /** Manages map messages */
  private def manageMapMessage(message: MapMessage, storemanager: ActorRef): Unit = {
    message match {
      // If it's a select command
      case SelectMapMessage(name: String) => {
        implicit val timeout = Timeout(25 seconds)
        // Ask the storemanager if there's a map with that name
        val future = storemanager ? new AskMapMessage(name)
        // When the storemanager answers
        future.map { result =>
          // If the answer is yes
          if (result.asInstanceOf[Boolean]) {
            selectedMap = name
            println("Map " + name + " selected")
          }
          else println("Invalid map")
        }
      }
      case _ => storemanager ! message
    }
  }

  /** Manages row messages */
  private def manageRowMessage(message: RowMessage, storemanager: ActorRef): Unit = {
    if (selectedMap != "")
      storemanager ! StorefinderRowMessage(selectedMap, message)
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