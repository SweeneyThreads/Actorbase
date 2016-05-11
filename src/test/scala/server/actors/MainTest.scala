package server.actors

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
import server.messages.query.user.DatabaseMessages.{CreateDatabaseMessage, SelectDatabaseMessage, ListDatabaseMessage}
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


  /*########################################################################
    Testing ListDatabaseMessage() receiving
    ########################################################################*/

  //creating a map that simulates a plausible list of databasename=>Storemanager
  val fakeStoremanagersMap=new ConcurrentHashMap[String,ActorRef]
  fakeStoremanagersMap.put("test", system.actorOf(Props[Storemanager]))
  fakeStoremanagersMap.put("biggetMapEu", system.actorOf(Props[Storemanager]))
  fakeStoremanagersMap.put("lastMapForNow", system.actorOf(Props[Storemanager]))
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
    val main = system.actorOf(Props(new Main(null, new FakeServerInjector {})))
    val future = main ? new ListDatabaseMessage()
    ScalaFutures.whenReady(future) {
      result => result should be("test biggetMapEu lastMapForNow ")
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
    val anotherMain = system.actorOf(Props(new Main(null, new FakeEmptyServerInjector {} )))
    val future2 = anotherMain ? new ListDatabaseMessage()
    ScalaFutures.whenReady(future2) {
      result => result should be("The server is empty")
    }
  }



  /*########################################################################
    Testing SelectDatabaseMessage() receiving
    ########################################################################*/

  /**
    * @todo qui controllo solo che il risponda una roba tipo "Database test selected", ma non che
    *       effettivamente selezioni il databse corretto, testa questa cosa è parecchio difficile
    *       segue la mia idea:
    *       -overridare il metodo <code>receive</code> dentro la classe <code>FakeTest</code>
    *       -fare in modo che questo override:
    *             -faccia quello che farebbe l'attore Main vero se riceve un SelectDataBaseMessage
    *             -inserire due nuovi messaggi uno per richiedere la mappa selezionata e uno per richiedere
    *              il database selzionato.
    */
  case class TestMessage(){}
  it should "select the correct database when recive a SelectDatabaseMessage(s: String)" in {
    class FakeMain extends Main(null, new FakeServerInjector {})
    val main3 = system.actorOf(Props(new FakeMain))
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

  /*########################################################################
    Testing CreateDatabaseMessage() receiving
    ########################################################################*/
  //it should "create a new"

}




