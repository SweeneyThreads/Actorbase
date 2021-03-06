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

import java.text.SimpleDateFormat
import java.util
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import server.enums.EnumReplyResult.Done
import server.enums.EnumStoremanagerType.StoremanagerType
import server.enums.{EnumReplyResult, EnumStoremanagerType}
import server.messages.query.ReplyMessage
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.RowMessages._

import scala.language.postfixOps

/**
  * Created by mattia on 29/05/2016.
  */
class StorefinderTest extends FlatSpec with Matchers with MockFactory {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._


  var configString = ConfigFactory.parseString("akka.loggers = [\"akka.event.slf4j.Slf4jLogger\"][\"akka.event.Logging$DefaultLogger\"]")
  var config = ConfigFactory.load(configString)
  configString = ConfigFactory.parseString("akka.loglevel = \"DEBUG\"")
  config = configString.withFallback(config)
  val System = ActorSystem("System",ConfigFactory.load(config))
  var log: LoggingAdapter = Logging.getLogger(System, this)
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  implicit val system = ActorSystem("System",ConfigFactory.load(config))

  /*########################################################################
    Testing correct log after no RowMessage receiving TU35
    ########################################################################*/
  /*testing if the storefinder returns the correct reply to yhe Main when reciving a ListKeysMessage*/


  "StorefinderActor" should "create the correct log line" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = System.actorOf(Props(classOf[Storemanager],new ConcurrentHashMap[String, Array[Byte]](), ("", null), EnumStoremanagerType.StorefinderType,null))
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
    val Pattern=(nowString+"\\....\\s.+\\sERROR\\sserver\\.actors\\.Storemanager\\s\\-\\sUnhandled\\smessage\\sin\\sactor;\\sakka:\\/\\/.+\\/.+\\/\\$.+,\\smethod:\\s.+").r
    //sleep for ensuring that the storefinder has product the log
    Thread.sleep(100)
    println(Pattern)
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
    val actorRef = TestActorRef(new Storemanager(new ConcurrentHashMap[String, Array[Byte]](), ("", null), EnumStoremanagerType.StorefinderType, null))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //clear default storekeeper
    val index1= ("","d")
    val index2 = ("d",null)
    val aux =new ConcurrentHashMap[String, Array[Byte]]()

    actor.leftChild = new actor.Child(System.actorOf(Props(classOf[FakeStoremanagerStorekeeper],aux, index1, EnumStoremanagerType.StorekeeperType,new Array[Byte](123),null)),index1,new util.ArrayList[ActorRef](0))
    actor.rightChild = new actor.Child(System.actorOf(Props(classOf[FakeStoremanagerStorekeeper],aux, index1, EnumStoremanagerType.StorekeeperType,new Array[Byte](123),null)),index2, new util.ArrayList[ActorRef](0))
    //add two fakestorekeepers
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

  it should "actually send the InsertRowMessage, UpdateRowMessage, RemoveRowMessage, FindRowMessage to correct storekeeper" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = TestActorRef(new Storemanager(new ConcurrentHashMap[String, Array[Byte]](), ("", null), EnumStoremanagerType.StorefinderType,null))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //clear default storekeeper
    //add two fakestorekeepers
    val index1= ("","c")
    val index2 = ("c",null)
    val value=new Array[Byte](123)
    val first =new Array[Byte](111)
    val second = new Array[Byte](222)
    val aux =new ConcurrentHashMap[String, Array[Byte]]()
    actor.leftChild = new actor.Child(System.actorOf(Props(classOf[FakeStoremanagerStorekeeper],aux, index1, EnumStoremanagerType.StorekeeperType,first,new Array[ActorRef](0))),index1,new util.ArrayList[ActorRef](0))
    actor.rightChild = new actor.Child(System.actorOf(Props(classOf[FakeStoremanagerStorekeeper],aux, index2, EnumStoremanagerType.StorekeeperType,second,new Array[ActorRef](0))),index2, new util.ArrayList[ActorRef](0))
    // now I send the message
    val InsertRowMessage1future = actorRef ? InsertRowMessage("c",value)
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(InsertRowMessage1future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,InsertRowMessage("c",value),FindInfo(first)))
    }
    val InsertRowMessage2future = actorRef ? InsertRowMessage("d",value)
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(InsertRowMessage2future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,InsertRowMessage("d",value),FindInfo(second)))
    }
    val UpdateRowMessage1future = actorRef ? UpdateRowMessage("a",value)
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(UpdateRowMessage1future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,UpdateRowMessage("a",value),FindInfo(first)))
    }
    val UpdateRowMessage2future = actorRef ? UpdateRowMessage("d",value)
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(UpdateRowMessage2future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,UpdateRowMessage("d",value),FindInfo(second)))
    }
    val RemoveRowMessage1future = actorRef ? RemoveRowMessage("a")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(RemoveRowMessage1future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,RemoveRowMessage("a"),FindInfo(first)))
    }
    val RemoveRowMessage2future = actorRef ? RemoveRowMessage("d")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(RemoveRowMessage2future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,RemoveRowMessage("d"),FindInfo(second)))
    }
    val FindRowMessage1future = actorRef ? FindRowMessage("a")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(FindRowMessage1future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,FindRowMessage("a"),FindInfo(first)))
    }
    val FindRowMessage2future = actorRef ? FindRowMessage("d")
    //when the message is completed i check that the StorefinderActor reply correctly
    ScalaFutures.whenReady(FindRowMessage2future) {
      result => result should be(ReplyMessage(EnumReplyResult.Done,FindRowMessage("d"),FindInfo(second)))
    }
  }
}

class FakeStoremanagerStorekeeper(data: ConcurrentHashMap[String,  Array[Byte]],index: (String, String), storemanagerType: StoremanagerType,wich: Array[Byte]=new Array[Byte](135),ninjas: util.ArrayList[ActorRef]) extends Storemanager(data,index,storemanagerType,ninjas){
  override def receive = {
    case m:ListKeysMessage => {
      val origSender = sender
      reply(ReplyMessage(Done, m, ListKeyInfo(List[String]("1", "2", "3"))), origSender)
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