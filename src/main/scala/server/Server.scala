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

import java.io.File

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.dispatch.ExecutionContexts._
import akka.event.{Logging, LoggingAdapter}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import server.DistributionStrategy.RoundRobinAddresses
import server.actors.{Doorkeeper, MapManager}
import server.utils.ConfigurationManager

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.language.postfixOps



/**
  * The Actorbase server, contains the main method of the server-side application of Actorbase.
  */
object Server {
  var log:LoggingAdapter = null
  // the SettingsManager of this node
  var settingsManager: ActorRef = null
  val configurationManager = new ConfigurationManager

  var clusterListener: ActorRef = null
  var sFclusterListener: ActorRef = null
  var sKclusterListener: ActorRef = null

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  def main(args: Array[String]) {
    configurationManager.readActorsProperties()
    // read distribution.conf file
    val newConfig = ConfigFactory.parseFile(new File("conf\\distribution.conf"))
    //merge the configurations
    val config = newConfig.withFallback(ConfigFactory.load())

    try {
      // Create the actor system
      val system = ActorSystem("ActorbaseSystem", ConfigFactory.load(config))
      // Create the cluster
      val cluster = Cluster(system)
      // If this node has a Doorkeeper node it creates doorkeepers
      if (cluster.selfRoles contains "Doorkeeper"){
        createDoorkeepers(system)
      }
      log = Logging.getLogger(system, this)

      // create the SettingsManager of this node
      settingsManager = system.actorOf(Props[SettingsManager])

      clusterListener = system.actorOf(Props[RoundRobinAddresses])
      sFclusterListener = system.actorOf(Props[RoundRobinAddresses])
      sKclusterListener = system.actorOf(Props[RoundRobinAddresses])

      loadDatabases(system)
      log.info("Server started")
    }
    catch {
      case e: Exception => {
        Console.println("[ERROR]: Port already in use, please change the " +
          "configuration file and try again.")
        System.exit(1);
      }
    }
  }

  /**
    * Loads all the databases.
    *
    * @param system The actor system.
    */
  private def loadDatabases(system: ActorSystem): Unit = {
    val folder = new File(StaticSettings.dataPath)
    //if the root dataFolder does not exist -> create it and put inside default databases
    if (!folder.exists) {
      folder.mkdir
      system.actorOf(Props[MapManager], name="test")
      system.actorOf(Props[MapManager], name="master")
      log.info("Generated default databases [- - - this should be the first time you launch ActorBase," +
        " or if you changed default dataFolder in the StaticSettings - - - ]")
    } else {
      // save the list of the folder inside the root dataFolder
      val dbsDirectory = folder.listFiles
      for(child <- dbsDirectory) {
        system.actorOf(Props[MapManager], name=child.getName)
      }
      log.info(s"Databases loaded from ${StaticSettings.dataPath}")
    }
  }
  /*private def loadDatabases(system: ActorSystem): Unit = {
    system.actorOf(Props(new MapManager("test")), name="test")
    system.actorOf(Props(new MapManager("master")), name="master")
    log.info("Databases loaded")
  }*/

  /**
    * Reads the doorkeeper's configuration file and creates the Doorkeeper
    *
    * @param system The actor system.
    */
  private def createDoorkeepers(system: ActorSystem): Unit ={
    try {
      val accesses = configurationManager.readDoorkeepersSettings("conf/ports.json")
      for (port <- accesses) {
        system.actorOf(Props(classOf[Doorkeeper], port))
      }
    }
    catch {
      case e:Exception => println("File not found")
    }
  }
}