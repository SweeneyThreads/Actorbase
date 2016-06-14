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

package server.messages.internal

import akka.actor.ActorRef

/**
  * ClusterListenerMessages are the messages that ClusterListener receives for future implementations.
  */
object ClusterListenerMessages {

  /**
    * Trait that all the messages that should be received from a ClusterListener must implement.
    */
  trait ClusterListenerMessage

  /**
    * A RoundRobinAddressMessage is used to ask to a RoundRobinAddresses the address to choose to deploy an actor.
    */
  case class RoundRobinAddressMessage() extends ClusterListenerMessage

  /**
    * A DatabaseAddedMessage is used to tell to the SettingsManager that there is a new database.
    * @param dbName name of the new db
    * @param mapManagerRef reference to the MapManager of the db
    */
  case class DatabaseAddedMessage(dbName: String, mapManagerRef: ActorRef) extends ClusterListenerMessage

  /**
    * A DatabaseAddedMessage is used to tell to the SettingsManager that a database has been removed.
    * @param dbName name of the removed db
    */
  case class DatabaseRemovedMessage(dbName: String) extends ClusterListenerMessage

  /**
    * A SettingsUpdatedMessage is used to tell to the SettingsManager that the settings has been updated.
    *
    * @param maxRowNumber new maxRowNumber value.
    * @param ninjaNumber new ninjaNumber value.
    * @param warehousemanNumber new warehousemanNumber value.
    */
  case class SettingsUpdatedMessage(maxRowNumber: Integer, ninjaNumber: Integer, warehousemanNumber: Integer  ) extends ClusterListenerMessage
}
