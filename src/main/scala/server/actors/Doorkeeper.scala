package server.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}

/**
  * Created by matteobortolazzo on 01/05/2016.
  */

/**
  * The actor that represents the entry point to the server.
  * It opens a port in the host and listens for new connections.
  * When a new client connects, the doorkeeper create an Usermanager actor
  * which will manage all the request from that client.
  * @param port
  */
class Doorkeeper(port: Integer) extends Actor with akka.actor.ActorLogging {

  import Tcp._
  import context.system
  IO(Tcp) ! Bind(self, new InetSocketAddress(port))

  /**
    * Processes messages in the mailbox.
    * Handles only messages from the Akka TCP actor.
    */
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
