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

import akka.actor.ActorRef
import server.messages.internal.ClusterListenerMessages.{SettingsUpdatedMessage, DatabaseRemovedMessage, DatabaseAddedMessage}

/**
  * Contains the list of reference of all StoreManager actors
  */
object StaticSettings   {
  var mapManagerRefs = new ConcurrentHashMap[String, ActorRef]()
  var maxRowNumber = 2
  var ninjaNumber = 1
  var warehousemanNumber = 1
  var dataPath = "d:\\data\\actorbase_data"

  /**
    * Method used to add a new database to the map.
    * It also communicate the insertion to the SettingsManager.
    *
    * @param name name of the new database.
    * @param actor reference to the MapManager actor that manages the database to replicate the change to other nodes.
    */
  def subscribe(name: String, actor: ActorRef): Unit ={
    // put the database inside the map
    mapManagerRefs.put(name,actor)
    // send a message to the SettingsManager.
    Server.settingsManager ! DatabaseAddedMessage(name,actor)
  }

  /**
    * Method used to remove a database from the map.
    * It also communicate the deletion to the SettingsManager to replicate the change to other nodes.
    *
    * @param name name of the deleted database.
    */
  def unSubscribe(name: String): Unit ={
    // remove the database from the map
    mapManagerRefs.remove(name)
    // send a message to the SettingsManager.
    Server.settingsManager ! DatabaseRemovedMessage(name)
  }

  /**
    * Method used to modify the settings.
    * It also communicate the changes to the SettingsManager to replicate the changes to other nodes.
    *
    * @param mRN new max number of rows.
    * @param nN new number of ninjas.
    * @param wN new number of warehoseman.
    */
  def changeSettings(mRN: Integer = maxRowNumber, nN: Integer = ninjaNumber, wN: Integer = warehousemanNumber): Unit ={
    // update settings
    maxRowNumber = mRN
    ninjaNumber = nN
    warehousemanNumber = wN
    // send a message to the SettingsManager.
    Server.settingsManager ! SettingsUpdatedMessage(mRN,nN,wN)
  }
}