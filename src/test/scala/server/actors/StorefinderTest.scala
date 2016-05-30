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

import java.text.SimpleDateFormat
import java.util.Calendar

/**
  * Created by mattia on 29/05/2016.
  */
class Storefinderest extends FlatSpec with Matchers with MockFactory {

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
    Testing correct log after no RowMessage receiving TU35
    ########################################################################*/
  /*testing if the storefinder returns the correct reply to yhe Main when reciving a ListKeysMessage*/


  it should "create the correct log line" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = TestActorRef(new Storefinder)
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // now I send the message
    val future = actorRef ? ListDatabaseMessage()
    // a take the time now
    val today = Calendar.getInstance.getTime
    // date format for year month and day
    val todayFormat = new SimpleDateFormat("yyyy-MM-dd")
    //create a String with today values
    val todayString :String = todayFormat.format(today)
    //get the today log file
    val source = scala.io.Source.fromFile("logs/actorbase."+todayString+".0.log")
    //get the string of all the file and close the file
    val lines = try source.mkString finally source.close()
    //date format for hours minutes and seconds
    val nowFormat = new SimpleDateFormat("HH:mm:ss")
    //create a String with now time
    val nowString = nowFormat.format(today)
    //regular expression that match the error log produced by the storefinder
    val Pattern=(nowString+"....\\s.+\\sERROR server\\.actors\\.Storefinder\\s\\-\\sUnhandled\\smessage" +
      "\\sin\\sactor;\\sakka:\\/\\/default\\/user\\/\\$\\$.+,\\smethod:\\s.+").r
    //if it doesn't find the line the log didn't appened correcly
    Pattern.findFirstIn(lines) shouldNot be (None)
  }

  /*########################################################################
    Testing ListKeysMessage() receiving TU36
    ########################################################################*/
  /*testing if the storefinder returns the correct reply to yhe Main when reciving a ListKeysMessage*/


  it should "return the correct concatenation of the storekeepers keys" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = TestActorRef(new Storefinder)
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //clear default storekeeper
    actor.storekeepers.clear()
    //add two fakestorekeepers
    actor.storekeepers.put(".*".r, System.actorOf(Props[FakeStorekeeper]))
    actor.storekeepers.put(".*".r, System.actorOf(Props[FakeStorekeeper]))
    // now I send the message
    val future = actorRef ? ListKeysMessage()
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done, ListKeysMessage(), ListKeyInfo(List[String]("1","1","2","2","3","3"))))
    }
  }

  /*########################################################################
    Testing InsertRowMessage, UpdateRowMessage, RemoveRowMessage, FindRowMessage receiving TU37
    ########################################################################*/
  /*testing if the storefinder returns the correct reply to yhe Main when reciving a ListKeysMessage*/

  it should "actually sed the InsertRowMessage, UpdateRowMessage, RemoveRowMessage, FindRowMessage to correct storekeeper" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = TestActorRef(new Storefinder)
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //clear default storekeeper
    actor.storekeepers.clear()
    //add two fakestorekeepers
    actor.storekeepers.put("[a-cA-C]".r, System.actorOf(Props[FakeStorekeeper1]))
    actor.storekeepers.put("[c-zC-Z]".r, System.actorOf(Props[FakeStorekeeper2]))
    // now I send the message
    val InsertRowMessage1future = actorRef ? InsertRowMessage("a","b")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(InsertRowMessage1future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,InsertRowMessage("a","b"),FindInfo("storekeeper1")))
    }
    val InsertRowMessage2future = actorRef ? InsertRowMessage("d","e")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(InsertRowMessage2future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,InsertRowMessage("d","e"),FindInfo("storekeeper2")))
    }
    val UpdateRowMessage1future = actorRef ? UpdateRowMessage("a","b")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(UpdateRowMessage1future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,UpdateRowMessage("a","b"),FindInfo("storekeeper1")))
    }
    val UpdateRowMessage2future = actorRef ? UpdateRowMessage("d","e")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(UpdateRowMessage2future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,UpdateRowMessage("d","e"),FindInfo("storekeeper2")))
    }
    val RemoveRowMessage1future = actorRef ? RemoveRowMessage("a")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(RemoveRowMessage1future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,RemoveRowMessage("a"),FindInfo("storekeeper1")))
    }
    val RemoveRowMessage2future = actorRef ? RemoveRowMessage("d")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(RemoveRowMessage2future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,RemoveRowMessage("d"),FindInfo("storekeeper2")))
    }
    val FindRowMessage1future = actorRef ? FindRowMessage("a")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(FindRowMessage1future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,FindRowMessage("a"),FindInfo("storekeeper1")))
    }
    val FindRowMessage2future = actorRef ? FindRowMessage("d")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(FindRowMessage2future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,FindRowMessage("d"),FindInfo("storekeeper2")))
    }
  }



}
class FakeStorekeeper extends Storekeeper{

  override def receive = {
    case m:ListKeysMessage => {
      val origSender = sender
      reply(ReplyMessage(EnumReplyResult.Done,m,ListKeyInfo(List[String]("1","2","3"))), origSender)
    }
  }

}

class FakeStorekeeper1 extends Storekeeper{

  override def receive = {
    case m: RowMessage =>{
      val origSender = sender
      reply(ReplyMessage(EnumReplyResult.Done,m ,FindInfo("storekeeper1")), origSender)
    }
  }

}

class FakeStorekeeper2 extends Storekeeper{

  override def receive = {
    case m: RowMessage => {
      val origSender = sender
      reply(ReplyMessage(EnumReplyResult.Done,m ,FindInfo("storekeeper2")), origSender)
    }
  }

}