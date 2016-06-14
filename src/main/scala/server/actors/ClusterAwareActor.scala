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

import akka.actor.{ActorRef, Actor, Address}
import akka.cluster.Cluster
import server.Server
import server.messages.internal.ClusterListenerMessages.RoundRobinAddressMessage
import scala.concurrent.duration._
import akka.dispatch.ExecutionContexts._
import akka.pattern.ask
import akka.util.Timeout

import scala.language.postfixOps


/**
  * Trait that gives to an actor the nextAddress method,
  * should be extended from actors that needs to create actors in other nodes of the cluster.
  * The policy of addresses selection is responsibility of the ClusterListener of this node.
  * It also includes the values required to use futures.
  *
  * @see ClusterListener
  */
trait ClusterAwareActor extends Actor {
  // Values for futures
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  var clusterListener: ActorRef = Server.clusterListener

  /**
    * Returns an address of one node in the cluster.
    * This method sends a message to the ClusterListener of the same node of this actor
    * so it will stop the thread of this actor while waiting for a response.
    *
    * @return the address of type akka.actor.Address.
    */
  def nextAddress: Address ={
    // chances to wait for a response
    var chances = 10
    // auxiliary variable
    var aux :Address = Cluster(context.system).selfAddress
    // send a message to the ClusterListener of this node to get an address
    clusterListener ? RoundRobinAddressMessage onSuccess {
      // save the response as an address
      case result => aux = result.asInstanceOf[Address]
    }
    // wait for the response from the ClusterListener
    while(aux == Cluster(context.system).selfAddress & chances > 0){
      Thread.sleep(10)
      chances = chances - 1
    }
    // return the address
    aux
  }
}
