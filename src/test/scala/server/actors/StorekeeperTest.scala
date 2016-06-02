package server.actors

import server.enums.EnumReplyResult
import server.messages.internal.BecomeStorekeeperMessage
import server.messages.query.ReplyMessage

import scala.language.postfixOps
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.{FlatSpec, Matchers}
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.testkit.TestActorRef
import server.messages.query.user.RowMessages._

import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.concurrent.ScalaFutures


/**
  * Created by mattia on 31/05/2016.
  */
class StorekeeperTest extends FlatSpec with Matchers with MockFactory {

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
    Testing InsertRowMessage() receiving TU39
    ########################################################################*/
  /*testing if the storekeeper insert the key and value, or reply with the correct error if the key already exist,
   when receiving an InsertRowMessage*/


   "StorekeeperActor" should "actually reply correctly if the storekeeper receives a InsertRowMessage with correct or incorrect key" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = TestActorRef(new Storekeeper(true))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
     val value=new Array[Byte](123)
    // now I send the message
    val future = actorRef ? InsertRowMessage("key",value)
    //when the message is completed i check that the StorekeeperActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new InsertRowMessage("key",value),null))
    }
     //check if the key and value are correctly inserted
     actor.db.get("key") should be(value)
    //insert a key in the db of the storekeeper
    actor.db.put("AlreadyExistingKey",value)
    // now I send the message to insert the already inserted key
    val future2 = actorRef ? InsertRowMessage("AlreadyExistingKey",value)
    //when the message is completed i check that the StorekeeperActor reply correctly
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new InsertRowMessage("AlreadyExistingKey",value),KeyAlreadyExistInfo()))
    }
  }
  /*########################################################################
  Testing UpdateRowMessage() receiving TU40
  ########################################################################*/
  /*testing if the storekeeper Update the key and value, or reply with the correct error if the key not exist,
   when receiving an UpdadeRowMessage*/


  it should "actually reply correctly if the storekeeper receives a UpdateRowMessage with correct or incorrect key" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = TestActorRef(new Storekeeper(true))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    val value=new Array[Byte](123)
    val value2=new Array[Byte](321)
    // now I send the message
    actor.db.put("key", value)
    val future = actorRef ? UpdateRowMessage("key", value2)
    //when the message is completed i check that the StorekeeperActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage(EnumReplyResult.Done, new UpdateRowMessage("key", value2), null))
    }
    actor.db.get("key") should be(value2)
    // now I send the message to update not inserted key
    val future2 = actorRef ? UpdateRowMessage("NotExistingKey", value)
    //when the message is completed i check that the StorekeeperActor reply correctly
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage(EnumReplyResult.Error, new UpdateRowMessage("NotExistingKey", value), KeyDoesNotExistInfo()))
    }
  }

  /*########################################################################
  Testing RemoveRowMessage() receiving TU41
  ########################################################################*/
  /*testing if the storekeeper remove the key and value, or reply with the correct error if the key not exist,
   when receiving a RemoveRowMessage*/


  it should "actually reply correctly if the storekeeper receives a RemoveRowMessage with correct or incorrect key" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = TestActorRef(new Storekeeper(true))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // now I send the message
    actor.db.put("key", "value".getBytes("UTF-8"))
    val future = actorRef ? RemoveRowMessage("key")
    //when the message is completed i check that the StorekeeperActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage(EnumReplyResult.Done, new RemoveRowMessage("key"), null))
    }
    actor.db.get("key") should be(null)
    // now I send the message to update not inserted key
    val future2 = actorRef ? RemoveRowMessage("NotExistingKey")
    //when the message is completed i check that the StorekeeperActor reply correctly
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage(EnumReplyResult.Error, new RemoveRowMessage("NotExistingKey"), KeyDoesNotExistInfo()))
    }
  }

  /*########################################################################
    Testing FindRowMessage() receiving TU42
    ########################################################################*/
  /*testing if the storekeeper return the value of the requested key, or reply with the correct error if the key Not exist,
   when receiving an FindRowMessage*/


  it should "actually reply correctly if the storekeeper receives a FindRowMessage with correct or incorrect key" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = TestActorRef(new Storekeeper(true))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    val value=new Array[Byte](123)
    //insert a key in the db of the storekeeper
    actor.db.put("key",value)
    // now I send the message
    val future = actorRef ? FindRowMessage("key")
    //when the message is completed i check that the StorekeeperActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new FindRowMessage("key"),FindInfo(value)))
    }
    // now I send the message to find a non existing key
    val future2 = actorRef ? FindRowMessage("NotExistingKey")
    //when the message is completed i check that the StorekeeperActor reply correctly
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new FindRowMessage("NotExistingKey"),KeyDoesNotExistInfo()))
    }
  }
  /*########################################################################
   Testing ListKeysMessage() receiving TU43
   ########################################################################*/
  /*testing if the storekeeper return the list of his keys when receiving an ListKeysMessage*/


  it should "actually reply correctly if the storekeeper receives a ListKeysMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = TestActorRef(new Storekeeper(true))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //insert some keys in the db of the storekeeper
    actor.db.put("key1","value1".getBytes("UTF-8"))
    actor.db.put("key2","value2".getBytes("UTF-8"))
    actor.db.put("key3","value3".getBytes("UTF-8"))
    actor.db.put("key4","value4".getBytes("UTF-8"))
    actor.db.put("key5","value5".getBytes("UTF-8"))
    actor.db.put("key6","value6".getBytes("UTF-8"))
    // now I send the message
    val future = actorRef ? ListKeysMessage()
    //when the message is completed i check that the StorekeeperActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new ListKeysMessage,ListKeyInfo(List[String]("key4","key3","key6","key5","key2","key1"))))
    }
  }
}