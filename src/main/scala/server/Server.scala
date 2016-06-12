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

import java.io.{File, FileNotFoundException}
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.dispatch.ExecutionContexts._
import akka.event.{Logging, LoggingAdapter}
import akka.remote.RemoteTransportException
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import server.actors.{Doorkeeper, MapManager}
import server.enums.EnumPermission
import server.enums.EnumPermission.UserPermission
import server.utils.ConfigurationManager

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.language.postfixOps



/**
  * The Actorbase server, contains the main method of the server-side application of Actorbase.
  */
object Server {
  var log:LoggingAdapter = null

  var clusterListener: ActorRef = null
  var sFclusterListener: ActorRef = null
  var sKclusterListener: ActorRef = null

  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  def main(args: Array[String]) {
    val conf = ConfigFactory.load()
    try {
      val system = ActorSystem("System", conf)
      log = Logging.getLogger(system, this)

      clusterListener = system.actorOf(Props[ClusterListener])
      sFclusterListener = system.actorOf(Props[ClusterListener])
      sKclusterListener = system.actorOf(Props[ClusterListener])

      loadDatabases(system)
      createDoorkeepers(system)
      log.info("Server started")
    }
      //TODO REMOVE that shit, it ain't workin'
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
    val confManager = new ConfigurationManager
    try {
      val accesses = confManager.readDoorkeepersSettings("conf/ports.json")
      for (port <- accesses) {
        system.actorOf(Props(classOf[Doorkeeper], port))
      }
    }
    catch {
      case e:Exception => println("File not found")
    }
  }
}

/* RICETTA PER LA FELICITA'

* Fai partire il server (Debug).
* Fai partire il client (Run).
* Scrivi 'connect localhost:8181 admin admin'
* Divertiti con i comandi che Borto ha implementato per te!
*
* listdb, selectdb nome, createdb nome, deletedb nome,
* listmap, selectmap nome, createmap nome, deletemap nome,
* insert 'key' value, update 'key' value, remove 'key', find 'key', keys.
*
* Quando vuoi disconnetterti scrivi 'disconnect'
*
* Il server per ora NON risponde al client MA viene tutto stampato su console.
*
* */

/*  COME è STRUTTURAT0 IL SERVER
*
* Il server crea un Doorkeeper (porta 8181) e uno Storemanager ("test"). **uno storemanager rappresenta una database**
* Il Doorkeeper, per ogni connessione, crea un Usermanager.
*
* Ogni stringa inviata dal un client verrà gestita dal proprio Usermanager.
* Quest'ultimo ha il compito di creare i messaggi (con l'aiuto di una classe di parsing),
* controllare i permessi e spedire ad un Main.
*
* Un Main gestisce i messaggi di un client.
* L'attore Main ha la responsabilità di gestire i database (Storemanager).
* L'attore Storemanager ha la responsabilità di gestire le mappe (Storefinder)*
* Storefinder e Storekeeper li conosciamo tutti.
*
* Ho implementato l'indicizzazione degli Storekeeper attraverso espressioni regolari.
*
* */