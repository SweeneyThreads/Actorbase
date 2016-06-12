/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 SWEeneyThreads
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 *
 * @author SWEeneyThreads
 * @version 0.0.1
 * @since 0.0.1
 */

package server.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import Tcp._

/**
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
  try {
    IO(Tcp) ! Bind(self, new InetSocketAddress(port))
  }
  catch {
    case e: java.net.BindException => log.error("Error, port already in use.")
  }

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
