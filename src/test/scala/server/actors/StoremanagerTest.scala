package server.actors

import server.enums.EnumReplyResult
import server.messages.query.ReplyMessage

import scala.language.postfixOps
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}
import java.util.concurrent.ConcurrentHashMap
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.MapMessages._
import server.utils.{ServerDependencyInjector}

import akka.testkit.TestActorRef

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
  Testing CreateMapMessage() receiving
  ########################################################################*/
  "Storemanager" should "reply correctly to a CreateMapMessage()" in {
    //creating a StoremanagerActor into system
    val storemanager = System.actorOf(Props(new Storemanager()))
    val future = storemanager ? new CreateMapMessage("prova")
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new CreateMapMessage("prova"),null))
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
  }
}

