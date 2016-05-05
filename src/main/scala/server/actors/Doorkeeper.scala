package server.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** Actor who manage new connections */
class Doorkeeper(port: Integer) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", port))

  def receive = {
    case b@Bound(localAddress) => println("Port " + port + " opened")
    case CommandFailed(_: Bind) => context stop self
    case c@Connected(remote, local) =>
      val connectionKeeper = context.actorOf(Props[Usermanager])
      val connection = sender()
      connection ! Register(connectionKeeper)
      println("Client connected")
  }
}
