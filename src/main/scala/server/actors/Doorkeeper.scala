package server.actors
import java.net.InetSocketAddress
import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import Tcp._

/**
  * Created by matteobortolazzo on 01/05/2016.
  * The actor that represents the entry point to the server.
  * It opens a port in the host and listens for new connections.
  * When a new client connects, the doorkeeper create an Usermanager actor
  * which will manage all the request from that client.
  *
  * @constructor Create a new Doorkeeper actor instance from an Integer.
  * @param port Integer that represents the port to use for manage the connection.
  */
class Doorkeeper(port: Integer) extends Actor with akka.actor.ActorLogging {

  import context.system
  IO(Tcp) ! Bind(self, new InetSocketAddress(port))

  /**
    * Processes all incoming messages.
    * It handles only messages coming from the TCP actor.
    * Handles Bound messages logging the opened port.
    * Handles CommandFailed killing itself.
    * Handles Connected messages creating an Usermanager actor for the new client.
    *
    * @see Usermanager
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
