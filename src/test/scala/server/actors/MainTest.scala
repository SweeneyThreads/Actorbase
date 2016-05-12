package server.actors

import java.lang.Enum
import java.util

import akka.actor.Actor.Receive
import akka.dispatch.ExecutionContexts._
import akka.util.Timeout
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}
import java.util.concurrent.ConcurrentHashMap
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import server.EnumPermission.Permission
import server.actors.{Doorkeeper, Storemanager}
import server.messages.query.user.DatabaseMessages.{DeleteDatabaseMessage, CreateDatabaseMessage, SelectDatabaseMessage, ListDatabaseMessage}
import server.messages.query.user.MapMessages.SelectMapMessage
import server.util.{ServerDependencyInjector, FileReader}
import server.{Server, EnumPermission}

import scala.concurrent.Future
import scala.util.{Failure, Success}

import akka.testkit.TestActorRef


/**
  * Created by kurt on 11/05/2016.
  */
class MainTest extends FlatSpec with Matchers with MockFactory{
  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._


  var System:ActorSystem = ActorSystem("System")
  var log:LoggingAdapter = Logging.getLogger(System, this)
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  implicit val system = ActorSystem()


  /*########################################################################
    Testing ListDatabaseMessage() receiving
    ########################################################################*/

  //creating a map that simulates a plausible list of databasename=>Storemanager
  val fakeStoremanagersMap=new ConcurrentHashMap[String,ActorRef]
  fakeStoremanagersMap.put("test", System.actorOf(Props[Storemanager]))
  fakeStoremanagersMap.put("biggestMapEu", System.actorOf(Props[Storemanager]))
  fakeStoremanagersMap.put("lastMapForNow", System.actorOf(Props[Storemanager]))
  //creating a fake server
  object FakeServer {
    var fakeStoremanagers: ConcurrentHashMap[String, ActorRef] = fakeStoremanagersMap
  }
  //creating the injector (the traits that tells the main actor i'll create soon to use FakeServer instead of the real server)
  trait FakeServerInjector extends ServerDependencyInjector {
    override def getStoremanagers : ConcurrentHashMap[String, ActorRef] = {
      FakeServer.fakeStoremanagers
    }
  }
  /*
  the test starts now:
  - first of all i put inside the system a new actor of tipe Main injecting it with the directive to use our FakeServer
  - then i send a ListDatabaseMessage to him and i store the result in a Future
  - when future is complete (this blocks the execution and waits the future to be completed) i check that the result is
    what i am expecting: the list of the names of the databases
   */
  "Main" should "reply correctly to a ListDatabaseMessage()" in {
    //creating a MainActor into system, injecting a dependency to a fake server that is defined above
    val main = System.actorOf(Props(new Main(null, new FakeServerInjector {})))
    val future = main ? new ListDatabaseMessage()
    ScalaFutures.whenReady(future) {
      result => result should be("test biggestMapEu lastMapForNow ")
    }
  }


  //like above
  //creating the fake server
  object FakeEmptyServer {
    var fakeStoremanagers=new ConcurrentHashMap[String,ActorRef] ()
  }
  //creating the injector for the fake server
  trait FakeEmptyServerInjector extends ServerDependencyInjector {
    override def getStoremanagers : ConcurrentHashMap[String, ActorRef] = {
      FakeEmptyServer.fakeStoremanagers
    }
  }
  //testing that the servers reacts well to ListDatabaseMessage even if empty
  it should "reply 'The server is empty' to a ListDatabaseMessage() if the server is in fact empty" in {
    val anotherMain = System.actorOf(Props(new Main(null, new FakeEmptyServerInjector {} )))
    val future2 = anotherMain ? new ListDatabaseMessage()
    ScalaFutures.whenReady(future2) {
      result => result should be("The server is empty")
    }
  }



  /*########################################################################
    Testing SelectDatabaseMessage() receiving
    ########################################################################*/
  /**
    * first of all I try to test what is possible to test without the akka-testkit, as reported
    * on the akka-testkit best practice
    *
    * @see [[http://doc.akka.io/docs/akka/current/scala/testing.html]]
   */
  class FakeMain extends Main(null, new FakeServerInjector {})
  it should "reply correctly when receiving a SelectDatabaseMessage(s: String) - - - database should not be actually selected, just testing correct answer" in {
    //a fake main with the desired injector
    val main3 = System.actorOf(Props(new FakeMain))
    val future3 = main3 ? SelectDatabaseMessage("test")
    ScalaFutures.whenReady(future3) {
      result => {
        result should be("Database test selected")
      }
    }
    //now i try to select a database that isn't actually inside our FakeServer
    val future = main3 ? SelectDatabaseMessage("aRandomDatabaseThatDontActuallyExists")
    ScalaFutures.whenReady(future) {
      result => {
        result should be("Invalid operation")
      }
    }
  }

  /**
    * now, using the akka-toolkit, I can test if the property of the actor actually change when
    * he receive the message
    * ''check this out:'' we can generate kind an actor ref from witch we can get the `underlyingActor` that
    * can be used to acces the actor's members.
    *
    * @note to TestActorRef you '''must''' put this line at the beginning of the scope: `implicit val system = ActorSystem()`
    *       @see [[http://stackoverflow.com/questions/35202570/could-not-find-implicit-value-for-parameter-system-akka-actor-actorsystem]]
    */
  it should "actually select the correct database when receiving a SelectDatabaseMessage(s: String)" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new FakeMain)
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // now I send the message
    val future = actorRef ? SelectDatabaseMessage("biggestMapEu")
    //when the message is completed i check that the MainActor property changed consistently
    ScalaFutures.whenReady(future) { result => {
      actor.selectedDatabase should be("biggestMapEu")
      actor.selectedMap should be("")
    }}
    // now i try to send a message with a wrong db name, and check that the selected map is the same than before
    val future2 = actorRef ? SelectDatabaseMessage("randomDatabaseThaDontExists")
    ScalaFutures.whenReady(future2) { result => {
      actor.selectedDatabase should be("biggestMapEu")
      actor.selectedMap should be("")
    }}
  }

  /*########################################################################
    Testing CreateDatabaseMessage() receiving
    ########################################################################*/
  /**
    * @todo test this soon
    */



    /*########################################################################
      Testing DeleteDatabaseMessage() receiving
      ########################################################################*/
    it should "actually delete from the StoreManagers map the database passed to 'deletedb' command" in {
      //refreshing the FakeServer
      FakeServer.fakeStoremanagers=fakeStoremanagersMap
      val actorRef=TestActorRef(new FakeMain)
      val actor = actorRef.underlyingActor
      //check that "test" is actually inside storemanagers map
      FakeServer.fakeStoremanagers.containsKey("test") should be(true)
      //issuing to remove "test"
      val future = actorRef ? DeleteDatabaseMessage("test")
      ScalaFutures.whenReady(future) { result => {
        //check that is no more inside storemanagers map
        FakeServer.fakeStoremanagers.containsKey("test") should be(false)
        result should be("Database test deleted")
      }}
      val future2 = actorRef ? DeleteDatabaseMessage("thisDbDoesNotExists")
      ScalaFutures.whenReady(future2) { result => {
        result should be("Invalid operation")
      }}
    }



  /*########################################################################
    Testing DeleteDatabaseMessage() receiving
    ########################################################################*/
  //refreshing the FakeServer
  FakeServer.fakeStoremanagers=fakeStoremanagersMap
  val actorRef=TestActorRef(new FakeMain)
  val actor = actorRef.underlyingActor

  //testing what happens if the mapName is valid
  //setting some preconditions
  actor.selectedDatabase="test"
  val future = actorRef ? SelectMapMessage("defaultMap")
  "Main (<- selectmap <mapName>)" should "actually set the correct map in his property (if mapName is valid)" in {
    ScalaFutures.whenReady(future) {result => {
      actor.selectedDatabase should be("test")
      actor.selectedMap should be("defaultMap")
    }}
  }
  it should "reply affirmatively when command is valid" in {
    ScalaFutures.whenReady(future) {result => {
      result should be("Map defaultMap selected")
    }}
  }


  //now testing what happens if the mapName is not valid
  //setting some preconditions
  actor.selectedDatabase = "test"
  actor.selectedMap = "defaultMap"
  val future2 = actorRef ? SelectMapMessage("imaginaryMap")

  it should "actually dont change selectedMap property if mapName in not valid" in {
    ScalaFutures.whenReady(future2) {result => {
      actor.selectedDatabase should be("test")
      actor.selectedMap should be("defaultMap")
    }}
  }
  it should "reply whit error when command is not valid" in {
    ScalaFutures.whenReady(future2) {result => {
      result should be("Invalid map")
    }}
  }

  //now testing what happens if there is no DB selected
  //setting some preconditions
  actor.selectedDatabase = ""
  actor.selectedMap = ""
  val future3 = actorRef ? SelectMapMessage("defaultMap")
  it should "if no DB selected -> dont change properties and reply with an error" in {
    ScalaFutures.whenReady(future3) {result => {
      actor.selectedDatabase should be("test")
      actor.selectedMap should be("defaultMap")
      result should be("Please select a database")
    }}
  }

}




