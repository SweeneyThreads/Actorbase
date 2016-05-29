package server.actors

import server.enums.EnumReplyResult
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
import server.utils.ServerDependencyInjector
import akka.testkit.TestActorRef
import server.enums.EnumReplyResult.Done
import server.messages.internal.AskMapMessage
import server.messages.query.user.RowMessages._

import scala.util.matching.Regex

/**
  * Created by mattia on 27/05/2016.
  */
class StoremanagerTest extends FlatSpec with Matchers with MockFactory {

  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._


  var System: ActorSystem = ActorSystem("System")
  var log: LoggingAdapter = Logging.getLogger(System, this)
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  implicit val system = ActorSystem()

  /*########################################################################
    Testing AskMapMessage() receiving
    ########################################################################*/
  /*testing if the storemanager returns the correct reply to yhe Main when reciving an AskMapMessage*/


  it should "actually return true if the storemanager contains the map asked with an AskMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Storemanager)
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // now I send the message
    val future = actorRef ? AskMapMessage("defaultMap")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result.asInstanceOf[Boolean] should be(true)
    }
    val future2 = actorRef ? AskMapMessage("NotexistingMap")
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future2) {
      result => result.asInstanceOf[Boolean] should be(false)
    }
  }


  /*########################################################################
    Testing ListMapMessage() receiving
    ########################################################################*/
  /*testing if the storemanager contains the defaultMap and if return a correct reply when receving a ListMapMessage*/


  it should "actually return correct maplist when receiving a ListMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Storemanager)
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // putting extra maps in the storefinders map
    actor.storefinders.put("map1",System.actorOf(Props[Storefinder]))
    actor.storefinders.put("map2",System.actorOf(Props[Storefinder]))
    // now I send the message
    val future = actorRef ? ListMapMessage()
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      val dbs = List[String]("defaultMap","map1","map2")
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new ListMapMessage(),ListMapInfo(dbs)))
    }
  }

  /*########################################################################
  Testing CreateMapMessage() receiving
  ########################################################################*/
  it should "create the correct map and reply correctly or send the correct error when receving a CreateMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Storemanager)
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // now I send the message
    val future = actorRef ? CreateMapMessage("map1")
    //when the message is completed i check that the StoremanagerActor reply correctly and delete correctly
    ScalaFutures.whenReady(future) {
      actor.storefinders.containsKey("map1") should be (true)
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new CreateMapMessage("map1"),null))
    }
    val future2 = actorRef ? CreateMapMessage("map1")
    //when the message is completed i check that the StoremanagerActor reply correctly that the map already exist
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new CreateMapMessage("map1"),MapAlreadyExistInfo()))
    }
  }



  /*########################################################################
   Testing DeleteMapMessage() receiving
   ########################################################################*/
  /*testing if the storemanager delete the correct map and reply correctly or send the correct error when receving a DeleteMapMessage*/


  it should "delete the correct map and reply correctly or send the correct error when receving a DeleteMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Storemanager)
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // putting extra maps in the storefinders map
    actor.storefinders.put("map1",System.actorOf(Props[Storefinder]))
    actor.storefinders.put("map2",System.actorOf(Props[Storefinder]))
    // now I send the message
    val future = actorRef ? DeleteMapMessage("map1")
    //when the message is completed i check that the StoremanagerActor reply correctly and delete correctly
    ScalaFutures.whenReady(future) {
      actor.storefinders.contains("map1") should be (false)
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new DeleteMapMessage("map1"),null))
    }
    val future2 = actorRef ? DeleteMapMessage("NotExistingMap")
    //when the message is completed i check that the StoremanagerActor reply correctly that the map not exist
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new DeleteMapMessage("NotExistingMap"),MapDoesNotExistInfo()))
    }
  }

  /*########################################################################
    Testing RowMapMessage() receiving
    ########################################################################*/
  /*testing if the storemanager receive and send correctly RowMapMessages and reply correctly*/



  it should "actually return the FakeStorefinder answer" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Storemanager)
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // putting extra FakeStorefinder map in the storefinders map
    actor.storefinders.put("map1",System.actorOf(Props[FakeStorefinder]))
    // now I send the message
    val future = actorRef ? StorefinderRowMessage("map1", FindRowMessage("1"))
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(ReplyMessage(Done, new FindRowMessage("1"), FindInfo("2")))
    }
    val future2 = actorRef ? StorefinderRowMessage("map2", FindRowMessage("1"))
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future2) {
      result => result should be(ReplyMessage(EnumReplyResult.Error,StorefinderRowMessage("map2" ,new FindRowMessage("1")), MapDoesNotExistInfo()))
    }
  }
}

/*FakeStoremanage for test porpuse always answers in the same way*/
class FakeStorefinder extends Storefinder{

  override def receive = {
    case m:RowMessage => {
      val origSender = sender
      reply(ReplyMessage(Done, new FindRowMessage("1"), FindInfo("2")), origSender)
    }
  }

}
