
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

import java.util

import com.typesafe.config.ConfigFactory
import server.DistributionStrategy.RoundRobinAddresses
import server.enums.EnumPermission.UserPermission
import server.messages.query.PermissionMessages.{NoReadPermissionInfo, NoWritePermissionInfo}
import server.{SettingsManager, Server, ClusterListener, StaticSettings}
import server.enums.{EnumPermission, EnumReplyResult, EnumStoremanagerType}
import server.messages.query.ReplyMessage

import scala.language.postfixOps
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.MapMessages._
import akka.testkit.TestActorRef
import server.enums.EnumReplyResult.Done
import server.enums.EnumStoremanagerType.StoremanagerType
import server.messages.internal.AskMessages.AskMapMessage
import server.messages.query.user.HelpMessages.{CompleteHelpMessage, CompleteHelpReplyInfo, SpecificHelpMessage, SpecificHelpReplyInfo}
import server.messages.query.user.RowMessages._
import server.utils.Helper


/**
  * Created by kurt on 11/05/2016.
  */
class MainTest extends FlatSpec with Matchers with MockFactory{
  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._


  var configString = ConfigFactory.parseString("akka.loggers = [\"akka.event.slf4j.Slf4jLogger\"][\"akka.event.Logging$DefaultLogger\"]")
  var config = ConfigFactory.load(configString)
  configString = ConfigFactory.parseString("akka.loglevel = \"DEBUG\"")
  config = configString.withFallback(config)
  val System = ActorSystem("System",ConfigFactory.load(config))
  var log: LoggingAdapter = Logging.getLogger(System, this)
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  implicit val system = ActorSystem("System",ConfigFactory.load(config))
  Server.settingsManager = System.actorOf(Props[SettingsManager])


  /*########################################################################
    Testing ListDatabaseMessage() receiving TU13
    ########################################################################*/

  //save the old static setting values
  val oldmapManagerRefs = StaticSettings.mapManagerRefs
  val oldmaxRowNumber = StaticSettings.maxRowNumber
  val oldninjaNumber = StaticSettings.ninjaNumber
  val oldwarehousemanNumber = StaticSettings.warehousemanNumber
  val olddataPath = StaticSettings.dataPath

  "main actor" should "actually return correct maplist when receiving a ListMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=system.actorOf(Props(classOf[Main],null))
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
    // now I send the message
    val future = actorRef ? ListDatabaseMessage()
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new ListDatabaseMessage(),NoDBInfo()))
    }
    StaticSettings.mapManagerRefs.clear()
    StaticSettings.mapManagerRefs.put("test1",System.actorOf(Props(classOf[FakeMapManager],new Array[Byte](1)),name="test1"))
    StaticSettings.mapManagerRefs.put("test2",System.actorOf(Props(classOf[FakeMapManager],new Array[Byte](1)),name="test2"))
    StaticSettings.mapManagerRefs.put("test3",System.actorOf(Props(classOf[FakeMapManager],new Array[Byte](1)),name="test3"))

    val future1 = actorRef ? ListDatabaseMessage()
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future1) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new ListDatabaseMessage(),ListDBInfo(List[String]("test1","test2","test3"))))
    }
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
  }

  /*########################################################################
    Testing SelectDatabaseMessage() receiving TU14
    ########################################################################*/

  it should "actually select the correct database or reply with the correct error" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Main(null))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
    // now I send the message
    val future = actorRef ? SelectDatabaseMessage("NotExistingDB")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new SelectDatabaseMessage("NotExistingDB"),DBDoesNotExistInfo()))
    }
    StaticSettings.mapManagerRefs.put("test12",System.actorOf(Props(classOf[FakeMapManager],new Array[Byte](1)),name="test12"))
    StaticSettings.mapManagerRefs.put("test22",System.actorOf(Props(classOf[FakeMapManager],new Array[Byte](1)),name="test22"))
    StaticSettings.mapManagerRefs.put("test32",System.actorOf(Props(classOf[FakeMapManager],new Array[Byte](1)),name="test32"))

    val future1 = actorRef ? SelectDatabaseMessage("test22")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future1) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new SelectDatabaseMessage("test22")))
        actor.selectedDatabase should be ("test22")
    }
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
  }
/*
  /*########################################################################
    Testing CreateDatabaseMessage() receiving TU15
    ########################################################################*/
    it should "create the correct database or reply with the correct error is the database already exsist" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Main(null))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    actor.clusterListener=System.actorOf(Props[RoundRobinAddresses])
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
    // now I send the message
    val future = actorRef ? CreateDatabaseMessage("NotExistingDB")
    //when the message is completed i check that the mainActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new CreateDatabaseMessage("NotExistingDB")))
        actor.selectedDatabase should be ("NotExistingDB")
    }
    StaticSettings.mapManagerRefs.put("AlreadyExistingDB",System.actorOf(Props[MapManager],name="AlreadyExistingDB"))

    val future1 = actorRef ? CreateDatabaseMessage("AlreadyExistingDB")
    //when the message is completed i check that the mainActor reply correctly
    ScalaFutures.whenReady(future1) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new CreateDatabaseMessage("AlreadyExistingDB"),DBAlreadyExistInfo()))
        actor.selectedDatabase should not be ("AlreadyExistingDB")
    }
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
  }
*/
  /*########################################################################
    Testing DeleteDatabaseMessage() receiving TU16
    ########################################################################*/

    it should "delete the correct database or reply with the correct error is the database not exsist" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Main(null))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
    // now I send the message
    val future = actorRef ? DeleteDatabaseMessage("NotExistingDB")
    //when the message is completed i check that the mainActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new DeleteDatabaseMessage("NotExistingDB"),DBDoesNotExistInfo()))
    }
    StaticSettings.mapManagerRefs.put("DB",System.actorOf(Props(classOf[FakeMapManager],new Array[Byte](1)),name="DB"))

    val future1 = actorRef ? DeleteDatabaseMessage("DB")
    //when the message is completed i check that the mainActor reply correctly
    ScalaFutures.whenReady(future1) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new DeleteDatabaseMessage("DB")))
        StaticSettings.mapManagerRefs.containsKey("DB") should be (false)
    }
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
  }

  /*########################################################################
    Testing SelectMapMessage() receiving TU17
    ########################################################################*/

  it should "actually select the correct map or reply with the correct error" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Main(null))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
    // now I send the message
    val future1 = actorRef ? SelectMapMessage("noDBselectedMap")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future1) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new SelectMapMessage("noDBselectedMap"),NoDBSelectedInfo()))
    }

    StaticSettings.mapManagerRefs.put("testdb",System.actorOf(Props(classOf[FakeMapManager],new Array[Byte](1)),name="testdb"))
    actor.selectedDatabase="testdb"
    val future2 = actorRef ? SelectMapMessage("NotExistingMap")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new SelectMapMessage("NotExistingMap"),MapDoesNotExistInfo()))
        actor.selectedMap should be ("")
    }

    val future3 = actorRef ? SelectMapMessage("existingmap")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future3) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new SelectMapMessage("existingmap")))
        actor.selectedMap should be ("existingmap")
    }
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
  }

  /*########################################################################
    Testing CompleteHelpMessage() SpecificHelpMessage() receiving TU18
    ########################################################################*/

  "main actor" should "actually return correct information when receiving a CompleteHelpMessage() or a SpecificHelpMessage()" in {
    val actorRef=system.actorOf(Props(classOf[Main],null))
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
    // now I send the message
    val helper= new Helper
    val future = actorRef ? CompleteHelpMessage()
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new CompleteHelpMessage(),CompleteHelpReplyInfo(helper.completeHelp())))
    }
    val command:String="Selectdb"
    val future1 = actorRef ? SpecificHelpMessage(command)
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future1) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new SpecificHelpMessage(command),SpecificHelpReplyInfo(helper.specificHelp(command))))
    }
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
  }

  /*########################################################################
    Testing RowMessage() receiving TU19
    ########################################################################*/

  it should "actually recive RowMessage() and send them to the correct MapManager" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Main(null))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
    // now I send the message
    val first=new Array[Byte](1)
    StaticSettings.mapManagerRefs.put("test13",System.actorOf(Props(classOf[FakeMapManager],first),name="test13"))
    actor.selectedDatabase="test13"
    actor.selectedMap="maptest"

    val future = actorRef ? FindRowMessage("existingmap")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new StorefinderRowMessage("maptest",FindRowMessage("existingmap")),FindInfo(first)))
    }
    //clear the map containing the databases
    StaticSettings.mapManagerRefs.clear()
  }


  /*########################################################################
    Testing UserMessage() write permission receiving TU20
    ########################################################################*/

  "main actor" should "reply with an error when the user try to do a query that needs write permmission without having them" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val userperm = new util.HashMap[String, UserPermission]
    userperm.put("nowritedatabase", EnumPermission.Read)
    val actorRef = TestActorRef(new Main(userperm))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor

    StaticSettings.mapManagerRefs.put("nowritedatabase", System.actorOf(Props(classOf[FakeMapManager],null), name = "nowritedatabase"))
    actor.selectedDatabase = "nowritedatabase"

    val future = actorRef ? DeleteDatabaseMessage("nowritedatabase")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage(EnumReplyResult.Error, new DeleteDatabaseMessage("nowritedatabase"), NoWritePermissionInfo()))
    }
    val future1 = actorRef ? CreateMapMessage("map")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future1) {
      result => result should be(new ReplyMessage(EnumReplyResult.Error, new CreateMapMessage("map"), NoWritePermissionInfo()))
    }
    val future2 = actorRef ? DeleteMapMessage("map")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage(EnumReplyResult.Error, new DeleteMapMessage("map"), NoWritePermissionInfo()))
    }
    actor.selectedMap = "existingmap"
    val value=new Array[Byte](2345)
    val future3 = actorRef ? InsertRowMessage("key",value )
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future3) {
      result => result should be(new ReplyMessage(EnumReplyResult.Error, new InsertRowMessage("key",value ), NoWritePermissionInfo()))
    }
    val future4 = actorRef ? UpdateRowMessage("key",value )
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future4) {
      result => result should be(new ReplyMessage(EnumReplyResult.Error, new UpdateRowMessage("key",value ), NoWritePermissionInfo()))
    }
    val future5 = actorRef ? RemoveRowMessage("key")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future5) {
      result => result should be(new ReplyMessage(EnumReplyResult.Error, new RemoveRowMessage("key"), NoWritePermissionInfo()))
    }
    StaticSettings.mapManagerRefs.clear()
  }

  /*########################################################################
    Testing UserMessage() read permission receiving TU21
    ########################################################################*/

  "main actor" should "reply with an error when the user try to do a query that needs read permission without having them" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val userperm = new util.HashMap[String, UserPermission]
    userperm.put("nowriteperdatabase", EnumPermission.Read)
    val actorRef = TestActorRef(new Main(userperm))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor

    StaticSettings.mapManagerRefs.put("noreaddatabase", System.actorOf(Props(classOf[FakeMapManager],null), name = "noreaddatabase"))
    StaticSettings.mapManagerRefs.put("nowriteperdatabase", System.actorOf(Props(classOf[FakeMapManager],null), name = "nowriteperdatabase"))

    val future = actorRef ? SelectDatabaseMessage("noreaddatabase")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage(EnumReplyResult.Error, new SelectDatabaseMessage("noreaddatabase"), NoReadPermissionInfo()))
    }

    val future1 = actorRef ? ListDatabaseMessage()
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future1) {
      result => result should be(new ReplyMessage(EnumReplyResult.Done, new ListDatabaseMessage(), ListDBInfo(List[String]("nowriteperdatabase"))))
    }
    StaticSettings.mapManagerRefs.clear()

  }
}


class FakeMapManager(val wich:Array[Byte]=null) extends ReplyActor{
  override def receive = {
    case AskMapMessage(name:String) => {
      val origSender = sender
      if(name=="existingmap")
        origSender ! true
      else
        origSender ! false
    }
    case m:RowMessage => {
      val origSender = sender
      reply(ReplyMessage(EnumReplyResult.Done,m,FindInfo(wich)), origSender)
    }
    case _ =>
      val origSender = sender
      reply(ReplyMessage(EnumReplyResult.Done,ListKeysMessage(),FindInfo(wich)), origSender)
  }

}



