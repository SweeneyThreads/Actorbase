package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString
import server.EnumPermission.Permission
import server.Server
import server.messages.{ConnectMessage, InvalidQueryMessage}
import server.util.Parser

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** Every client talks with an instance of this actor.
  * It creates messages from strings.
  * */
class Usermanager extends Actor {
  import Tcp._

  var mainActor: ActorRef = null
  var user: String = null
  var userPermissions: ConcurrentHashMap[String, Permission] = null
  var parser = new Parser()

  def receive = {
    case Received(data: ByteString) => {
      // Takes the first byte
      val op = data.slice(0, 1).toArray
      // If it's a query message (first byte = 1)
      if(op(0) == 1) {
        // Takes the query
        val query  = data.slice(1, data.length).utf8String
        // Parse the query
        val message = parser.parseQuery(query)
        message match {
          // If it's an invalid query
          case i: InvalidQueryMessage => println("Invalid query")
          //If it's a valid query
          case _ => {
            message match {
              // If it's a connection message
              case ConnectMessage(username, password) => {
                // If the user not connected yet
                if (user == null) {
                  val ps = Server.users.get(username)
                  // If it's a valid login
                  if (ps != null && ps == password) {
                    user = username
                    // Set permissions
                    userPermissions = Server.permissions.get(user)
                    // Create a main
                    mainActor = context.actorOf(Props(new Main(userPermissions)))
                    println(user + " is connected")
                  }
                  else println("Invalid login")
                }
                else println("You're already connected")
              }
              case _ => {
                // If the user is connected
                if (user != null) mainActor ! message
                else println("WTF?! You can't be here if you're using our client, " +
                  "so FUCK YOU and you're shitty home made actor-fuckin'-base client!")
              }
            }
          }
        }
      }
    }
    case PeerClosed => {
      println("Client disconnected")
      context stop self
    }
  }
}

//sender() ! Write(data) // risponde al client
