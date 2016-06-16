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
import server.messages.internal.ClusterListenerMessages.ClusterListenerMessage

/**
  * Trait actor responsible of keeping the addresses of the nodes marked as UP in the cluster.
  * There should be at least one ClusterListener actor on every node of the cluster.
  * This actor is designed to be extended by actors who needs to know about what is happening in the cluster
  */
trait ClusterListener extends Actor with ActorLogging{
  // cluster
  private val cluster = Cluster(context.system)

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
    // a new node is up in the cluster
    case MemberUp(member) =>
      // if the address is not a duplicate
      if(!addresses.contains(member.address)) {
        // the new node address is added to addresses list
        addresses.add(member.address)
        // action on memberUp notify
        memberUpAction(member.address)
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
          // action on unreachableMember notify
          unreachableMemberAction(member.address)
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
          // action on memberRemoved notify
          memberRemovedAction(member.address)
        }
      }
    // Any other message received is handled by handleCustomMessage function
    case msg: ClusterListenerMessage => handleCustomMessage(msg)
  }

  /**
    * Hook function for handling a custom message from classes that extends ClusterListener
    *
    * @param msg the message received of type ClusterListenerMessage
    */
  def handleCustomMessage(msg: ClusterListenerMessage)

  /**
    * Hook function to perform a custom action when a new member is up in cluster
    *
    * @param address address of the node
    */
  def memberUpAction(address: Address)

  /**
    * Hook function to perform a custom action when a member in the cluster becomes unreachable
    *
    * @param address address of the node
    */
  def unreachableMemberAction(address: Address)

  /**
    * Hook function to perform a custom action when a member is removed from cluster
    *
    * @param address address of the node
    */
  def memberRemovedAction(address: Address)
}