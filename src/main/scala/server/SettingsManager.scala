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

import java.util.concurrent.ConcurrentHashMap

import akka.actor._
import akka.remote.RemoteScope
import server.messages.internal.ClusterListenerMessages.{SettingsUpdatedMessage, ClusterListenerMessage, DatabaseRemovedMessage, DatabaseAddedMessage}

/**
  * RemoteAssistant is an actor that the SettingsManager deploys on every node up in cluster.
  * His duty is to receive the changes to StaticSettings from his father and to reflect them in his current node.
  */
class RemoteAssistant extends Actor{
  /**
    * Overriding of the akka.actor.Actor receive method.
    * It handles messages of type ClusterListenerMessage.
    */
  def receive = {
    // if the message is a DatabaseAddedMessage
    case msg: DatabaseAddedMessage =>
      // check if the database already exists in the node
      if(StaticSettings.mapManagerRefs.get(msg.dbName) == null){
        // if it doesn't put the database inside mapManagerRefs
        StaticSettings.mapManagerRefs.put(msg.dbName, msg.mapManagerRef)
      }
    // if the message is a DatabaseRemovedMessage
    case msg: DatabaseRemovedMessage =>
      // check if the database exists
      if(StaticSettings.mapManagerRefs.get(msg.dbName) != null){
        // if it does remove it from mapManagerRefs
        StaticSettings.mapManagerRefs.remove(msg.dbName)
      }
    // if the message is a SettingsUpdatedMessage
    case msg: SettingsUpdatedMessage =>
      // change all the settings with the ones from the message
      StaticSettings.maxRowNumber = msg.maxRowNumber
      StaticSettings.ninjaNumber = msg.ninjaNumber
      StaticSettings.warehousemanNumber = msg.warehousemanNumber
  }
}

/**
  * SettingsManager is an actor that keeps the StaticSettings equal in all nodes of the cluster.
  * To do that he deploys a RemoteAssistant in every node of the cluster ad keeps their references.
  * Once a static setting is changed he is notified thanks to a ClusterListenerMessage and he
  * forward it to all his assistants.
  */
class SettingsManager extends ClusterListener {
  // map of addresses of the nodes and the assistant deployed on it
  val remoteAssistantMap = new ConcurrentHashMap[Address, ActorRef]()

  /**
    * Overriding of the ClusterListener's method that is needed to receive ClusterListenerMessages.
    * This method forwards all the messages to all the assistants.
    *
    * @param msg the message received of type ClusterListenerMessage.
    */
  override def handleCustomMessage(msg: ClusterListenerMessage): Unit = {
   notifyAllAssistants(msg)
  }

  /**
    * Overriding of the ClusterListener's method that is invoked when a member joins the cluster and is moved to up.
    * This method deploys an assistant on the new node and saves his reference. Then the assistant is
    * initialized with the notifyNewAssistant method.
    *
    * @param address address of the node.
    */
  override def memberUpAction(address: Address): Unit = {
    // deploy a new RemoteAssistant in the new node and save his reference
    remoteAssistantMap.put(address, context.system.actorOf(Props[RemoteAssistant].withDeploy(Deploy(scope = RemoteScope(address)))))
    // initialize the assistant
    notifyNewAssistant(remoteAssistantMap.get(address))
  }

  /**
    * Overriding of the ClusterListener's method that is invoked when a member of the cluster becomes unreachable.
    * This method removes the assistant of the unreachable node from the map.
    *
    * @param address address of the node
    */
  override def unreachableMemberAction(address: Address): Unit = {
    // remove the entry of the node
    remoteAssistantMap.remove(address)
  }

  /**
    * Overriding of the ClusterListener's method that is invoked when a member of the cluster is removed.
    * This method removes the assistant of the removed node from the map.
    *
    * @param address address of the node
    */
  override def memberRemovedAction(address: Address): Unit = {
    // remove the entry of the node
    remoteAssistantMap.remove(address)
  }

  /**
    * Method used to initialize a new assistant.
    * A new assistant is created when a new node is moved to up, so he must update the node's settings.
    * To do that the SettingsManager sends to him a message of database insertion for every database in the cluster.
    *
    * @param ref reference to the new RemoteAssistant deployed.
    */
  private def notifyNewAssistant(ref: ActorRef): Unit = {
    // for every db in mapManagerRefs
    for (i <- StaticSettings.mapManagerRefs.keySet().toArray()) {
      // send a message to the new Assistant
      ref ! DatabaseAddedMessage(i.asInstanceOf[String], StaticSettings.mapManagerRefs.get(i))
    }
  }

  /**
    * Method used to forward the incoming messages to all the assistants.
    *
    * @param msg the incoming message.
    */
  private def notifyAllAssistants(msg: ClusterListenerMessage): Unit ={
    // for every remote assistant
    for(key <- remoteAssistantMap.keySet.toArray)
      // forward the message
      remoteAssistantMap.get(key) ! msg
  }
}
