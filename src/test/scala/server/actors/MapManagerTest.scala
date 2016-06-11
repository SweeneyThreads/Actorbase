

package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import server.ClusterListener
import server.enums.{EnumStoremanagerType, EnumReplyResult}
import server.enums.EnumReplyResult.Done
import server.enums.EnumStoremanagerType.StoremanagerType
import server.messages.internal.AskMessages.AskMapMessage
import server.messages.query.ReplyMessage
import server.messages.query.user.MapMessages._
import server.messages.query.user.RowMessages._

import scala.language.postfixOps

/**
  * Created by mattia on 27/05/2016.
  */
class MapManagerTest extends FlatSpec with Matchers with MockFactory {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  var System: ActorSystem = ActorSystem("System")
  var log: LoggingAdapter = Logging.getLogger(System, this)
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  val newconf =ConfigFactory.parseString("akka.remote.netty.tcp.port=0")
  val conf = ConfigFactory.load()
  val merge = newconf.withFallback(conf)
  val complete = ConfigFactory.load(merge)
  implicit val system = ActorSystem("System",complete)

  /*########################################################################
    Testing AskMapMessage() receiving TU52
    ########################################################################*/
  /*testing if the storemanager returns the correct reply to yhe Main when receiving an AskMapMessage*/


  "StoremanagerActor" should "actually return true if the storemanager contains the map asked with an AskMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new MapManager("test"))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    //put a storefinder in storefinders map
    val index1= ("","d")
    val aux =new ConcurrentHashMap[String, Array[Byte]]()
    actor.indexManagers.put("defaultMap", system.actorOf(Props(classOf[FakeStoremanagerStorefinder],aux, index1, EnumStoremanagerType.StorekeeperType,new Array[Byte](123),null)))
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
    Testing ListMapMessage() receiving TU53
    ########################################################################*/
  /*testing if the storemanager contains the defaultMap and if return a correct reply when receiving a ListMapMessage*/


  it should "actually return correct maplist when receiving a ListMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new MapManager("test"))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // putting extra maps in the storefinders map
    val index1= ("","d")
    val aux =new ConcurrentHashMap[String, Array[Byte]]()
    actor.indexManagers.put("map1",System.actorOf(Props(classOf[FakeStoremanagerStorefinder],aux, index1, EnumStoremanagerType.StorekeeperType,new Array[Byte](123),null)))
    actor.indexManagers.put("map2",System.actorOf(Props(classOf[FakeStoremanagerStorefinder],aux, index1, EnumStoremanagerType.StorekeeperType,new Array[Byte](123),null)))
    // now I send the message
    val future = actorRef ? ListMapMessage()
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new ListMapMessage(),ListMapInfo(List[String]("map1","map2"))))
    }
  }

  /*########################################################################
  Testing CreateMapMessage() receiving TU54
  ########################################################################
  testing if the storemanager create the map and reply correctly or reply with the correct error if the map already exist*/
  it should "create the correct map and reply correctly or send the correct error when receving a CreateMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new MapManager("test"))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    actor.clusterListener=System.actorOf(Props[ClusterListener])
    // now I send the message
    val future = actorRef ? CreateMapMessage("map1")
    //when the message is completed i check that the StoremanagerActor reply correctly and delete correctly
    ScalaFutures.whenReady(future) {
      //check if the map was correctly created
      actor.indexManagers.containsKey("map1") should be (true)
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new CreateMapMessage("map1"),null))
    }
    val future2 = actorRef ? CreateMapMessage("map1")
    //when the message is completed i check that the StoremanagerActor reply correctly that the map already exist
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new CreateMapMessage("map1"),MapAlreadyExistInfo()))
    }
  }



  /*########################################################################
   Testing DeleteMapMessage() receiving TU55
   ########################################################################*/
  /*testing if the storemanager delete the correct map and reply correctly or send the correct error when receving a DeleteMapMessage*/


  it should "delete the correct map and reply correctly or send the correct error when receving a DeleteMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new MapManager("test"))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // putting extra maps in the storefinders map
    val index1= ("","d")
    val aux =new ConcurrentHashMap[String, Array[Byte]]()
    actor.indexManagers.put("map1",System.actorOf(Props(classOf[FakeStoremanagerStorefinder],aux, index1, EnumStoremanagerType.StorekeeperType,new Array[Byte](123),null)))
    actor.indexManagers.put("map2",System.actorOf(Props(classOf[FakeStoremanagerStorefinder],aux, index1, EnumStoremanagerType.StorekeeperType,new Array[Byte](123),null)))

    // now I send the message
    val future = actorRef ? DeleteMapMessage("map1")
    //when the message is completed i check that the StoremanagerActor reply correctly and delete correctly
    ScalaFutures.whenReady(future) {
      actor.indexManagers.contains("map1") should be (false)
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new DeleteMapMessage("map1"),null))
    }
    val future2 = actorRef ? DeleteMapMessage("NotExistingMap")
    //when the message is completed i check that the StoremanagerActor reply correctly that the map not exist
    ScalaFutures.whenReady(future2) {
      result => result should be(new ReplyMessage (EnumReplyResult.Error,new DeleteMapMessage("NotExistingMap"),MapDoesNotExistInfo()))
    }
  }

  /*########################################################################
    Testing RowMapMessage() receiving TU56
    ########################################################################*/
  /*testing if the storemanager receive and send correctly RowMapMessages and reply correctly*/



  it should "actually return the FakeStorefinder answer" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new MapManager("test"))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    val value=new Array[Byte](123)
    // putting extra FakeStorefinder map in the storefinders map
    val index1= ("","d")
    val aux =new ConcurrentHashMap[String, Array[Byte]]()
    actor.indexManagers.put("map1",System.actorOf(Props(classOf[FakeStoremanagerStorefinder],aux, index1, EnumStoremanagerType.StorekeeperType,value,null)))
    // now I send the message
    val future = actorRef ? StorefinderRowMessage("map1", FindRowMessage("1"))
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(ReplyMessage(Done, new FindRowMessage("1"), FindInfo(value)))
    }
    val future2 = actorRef ? StorefinderRowMessage("map2", FindRowMessage("1"))
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future2) {
      result => result should be(ReplyMessage(EnumReplyResult.Error,StorefinderRowMessage("map2" ,new FindRowMessage("1")), MapDoesNotExistInfo()))
    }
  }
}
/**fake storefinder for receiving RowMessage test,
  * it builds with the value it has to return
  * */
class FakeStoremanagerStorefinder(data: ConcurrentHashMap[String,  Array[Byte]],index: (String, String), storemanagerType: StoremanagerType,returnInfo: Array[Byte]=new Array[Byte](135),ninjas: Array[ActorRef]) extends Storemanager(data,index,storemanagerType,ninjas){


  override def receive = {
    case m:RowMessage => {
      val origSender = sender
      reply(ReplyMessage(Done, new FindRowMessage("1"), FindInfo(returnInfo)), origSender)
    }
  }
}