package server.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/** Actor who manage new connections */
class Doorkeeper(port: Integer) extends Actor with akka.actor.ActorLogging {

  import Tcp._
  import context.system
  IO(Tcp) ! Bind(self, new InetSocketAddress(port))

  def receive = {
    // When the socket is opened
    case b@Bound(localAddress) => log.info("Port " + port + " opened")
    // When a command failed
    case CommandFailed(_: Bind) => context stop self
    // When a client connects for the first time
    case c@Connected(remote, local) => {
      // Create an usermanager
      val usermanager = context.actorOf(Props[Usermanager])
      val connection = sender()
      // Bind the client to the usermanager
      connection ! Register(usermanager)
      log.info(remote.getHostName + " connected")
    }
  }
}
