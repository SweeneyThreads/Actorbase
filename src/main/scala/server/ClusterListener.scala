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

import java.util.ArrayList

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

/**
  * Actor responsible of keeping the addresses of the nodes marked as UP in the cluster.
  * There should be one ClusterListener actor on every node of the cluster.
  * This actor also offer a Round Robin strategy to select an address from his list of nodes.
  */
class ClusterListener extends Actor with ActorLogging{
  // cluster
  private val cluster = Cluster(context.system)
  // number of the nodes UP in cluster, initially 0
  private var nNodes: Integer = 0
  // counter of requests. initially 0. It must be incremented before the % operation
  var counter: Integer = 0
  // list of the addresses of the nodes in the cluster
  var addresses: ArrayList[Address] = new ArrayList()

  /**
    * Overriding of the preStart method of Actor.
    * When created this actor subscribes itself to cluster.
    * It also adds the address of his node to his list.
    */
  override def preStart(): Unit = {
    // subscription to cluster events
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
    // append to the list the address of this node
    addresses.add(cluster.selfAddress)
    // update the number of up nodes
    nNodes = addresses.size()
  }

  /**
    * Overriding of the postStop method of Actor.
    * When he is stopped he unsubscribes itself from cluster.
    */
  override def postStop(): Unit = cluster.unsubscribe(self)

  /**
    * Receive method of the actor.
    * This method receives gossip from cluster and the string "next".
    * A "next" message is a request for an address given by nextAddress method.
    * The messages from the cluster is used to keep updated the list of node addresses and the number of nodes.
    */
  def receive = {
    // request of an address
    case "next" =>
      sender() ! nextAddress()
    // a new node is up in the cluster
    case MemberUp(member) =>
      // if the address is not a duplicate
      if(!addresses.contains(member.address)) {
        // the new node address is added to addresses list
        addresses.add(member.address)
        // update the number of up nodes
        nNodes = addresses.size
      }
    // a node has been marked as unreachable so it must be removed from the list of the addresses
    // if the node will return up it will be added again
    case UnreachableMember(member) =>
      // iterator for addresses
      val it = addresses.iterator()
      // loop on the list
      while(it.hasNext) {
        // if the address of the unreachable node is found
        if (it.next() == member.address) {
          //remove the address
          it.remove()
          // update the number of up nodes
          nNodes = addresses.size
        }
      }
    // a node has been removed
    case MemberRemoved(member, previousStatus) =>
      // iterator for addresses
      val it = addresses.iterator()
      // loop on the list
      while(it.hasNext) {
        // if the address of the removed node is found
        if (it.next() == member.address) {
          //remove the address
          it.remove()
          // update the number of up nodes
          nNodes = addresses.size
        }
      }
    case _ => // ignore
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
    // counter % nNodes will select addresses in a Round Robin way and returns it
    addresses.get(counter%nNodes)
  }

}