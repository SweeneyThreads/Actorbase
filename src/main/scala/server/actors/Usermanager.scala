package server.actors

import server.util.Parser
import akka.actor.{Actor, Props}
import akka.io.Tcp
import akka.util.ByteString
import server.messages.InvalidQueryMessage

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** Every client talks with an instance of this actor */
class Usermanager extends Actor {
  import Tcp._
  val mainActor = context.actorOf(Props[Main])
  def receive = {
    case Received(data: ByteString) => {
      val op =data.slice(0, 1).toArray // 1 = query

      if(op(0) == 1) {
        val query  = data.slice(1, data.length).utf8String
        val message = Parser.parseQuery(query)
        val valid = message match {
          case i:InvalidQueryMessage => false
          case _ => true
        }
        if(valid) mainActor ! message
        else println("Invalid query")
      }
      //sender() ! Write(data) // risponde al client
    }
    case PeerClosed     => context stop self
  }
}
