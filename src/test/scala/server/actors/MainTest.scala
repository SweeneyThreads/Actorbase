package server.actors

import akka.actor.Actor.Receive
import akka.dispatch.ExecutionContexts._
import akka.util.Timeout
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}
import java.util.concurrent.ConcurrentHashMap
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import server.EnumPermission.Permission
import server.actors.{Doorkeeper, Storemanager}
import server.messages.query.user.DatabaseMessages.ListDatabaseMessage
import server.util.{ServerDependencyInjector, FileReader}
import server.{Server, EnumPermission}

import scala.concurrent.Future
import scala.util.{Failure, Success}


/**
  * Created by kurt on 11/05/2016.
  */
class MainTest extends FlatSpec with Matchers with MockFactory{
  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._


  var system:ActorSystem = ActorSystem("System")
  var log:LoggingAdapter = Logging.getLogger(system, this)
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global




  //a couple fake map with fake Storemanagers
  val fakeStoremanagersMap=new ConcurrentHashMap[String,ActorRef]
  fakeStoremanagersMap.put("test", system.actorOf(Props[Storemanager]))
  fakeStoremanagersMap.put("biggetMapEu", system.actorOf(Props[Storemanager]))
  fakeStoremanagersMap.put("lastMapForNow", system.actorOf(Props[Storemanager]))
  //val fakeEmptyStoremanagersMap = new ConcurrentHashMap[String,ActorRef]
  //a couple of fake server
  object FakeServer {
    var fakeStoremanagers: ConcurrentHashMap[String, ActorRef] = fakeStoremanagersMap
  }
  /*object FakeEmptyServer {
    var fakeStoremanagers: ConcurrentHashMap[String, ActorRef] = fakeEmptyStoremanagersMap
  }*/
  //the injector for the fake servers
  trait FakeServerInjector extends ServerDependencyInjector {
    override def getStoremanagers : ConcurrentHashMap[String, ActorRef] = {
      FakeServer.fakeStoremanagers
    }
  }
  /*trait FakeEmptyServerInjector extends ServerDependencyInjector {
    override def getStoremanagers : ConcurrentHashMap[String, ActorRef] = {
      FakeEmptyServer.fakeStoremanagers
    }
  }*/

  "Main" should "reply correctly to a ListDatabaseMessage()" in {
    //creating a MainActor into system, injecting a dependency to a fake server that is defined above
    val main = system.actorOf(Props(new Main(null, new FakeServerInjector {})))
    val future = main ? new ListDatabaseMessage()
    future.onComplete {
      case Success(result) => {
        //result.toString should be("random string")
        result should be("sdasdasdsadasad")
      }
      case Failure(t) => {
        print("culo")
      }
    }
  }
/*
  it should "reply 'The server is empty' to a ListDatabaseMessage() if the server is really empty" in {
    val anotherMain = system.actorOf(Props(new Main(null, new FakeEmptyServerInjector {})))
    val future2 = anotherMain ? new ListDatabaseMessage()
    future2.onComplete {
      case Success(res) => {
        res should be("The server is empty")
      }
      case Failure(t) => {
        //fail("messager never replyed")
      }
    }
  }
*/



}




