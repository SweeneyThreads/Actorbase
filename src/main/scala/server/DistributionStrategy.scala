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

package server

import akka.actor.Address
import server.messages.internal.ClusterListenerMessages.{ClusterListenerMessage, RoundRobinAddressMessage}

/**
  * DistributionStrategy are used to manage the location of actors in the Cluster.
  */
object DistributionStrategy {

  /**
    * RoundRobinAddresses is a class that offers a RoundRobin strategy to select.
    * the next address for actor deployment.
    */
  class RoundRobinAddresses extends ClusterListener{
    //counter of requests. initially 0. It must be incremented before the % operation
    var counter: Integer = 0

    /**
      * Overriding of the abstract function of ClusterListener.
      * This let RoundRobinAddresses receive messages of Any type.
      * The only type needed is RoundRobinAddress message.
      *
      * @param msg the message received of Any type.
      */
    override def handleCustomMessage(msg: ClusterListenerMessage): Unit ={
      msg match {
        // if it is a RoundRobinAddress reply with next address
        case RoundRobinAddressMessage() => sender() ! nextAddress()
        //else ignore the message
        case _ => //ignore
      }
    }

    // do nothing when a new member joins cluster
    override def memberUpAction(msg: Address): Unit = {
      // do nothing
    }
    // do nothing when a member of the cluster becomes unreachable
    override def unreachableMemberAction(msg: Address): Unit = {
      // do nothing
    }
    // do nothing when a member is removed from cluster
    override def memberRemovedAction(msg: Address): Unit = {
      // do nothing
    }

  /**
    * This method is a Round Robin strategy to select an address from the list.
    * It uses the module of counter of requests and number of nodes.
    *
    * @return the address chosen of type akka.actor.Address.
    */
    def nextAddress(): Address = {
      // increment the number of requests
      counter=counter+1
      // counter % number of nodes will select addresses in a Round Robin way and returns it
      addresses.get(counter % addresses.size())
    }
  }

}
