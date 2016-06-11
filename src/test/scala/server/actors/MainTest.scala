
package server.actors

import com.typesafe.config.ConfigFactory
import server.enums.{EnumStoremanagerType, EnumReplyResult}
import server.messages.query.ReplyMessage

import scala.language.postfixOps
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}
import java.util.concurrent.ConcurrentHashMap
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.MapMessages.{ListMapInfo, ListMapMessage, SelectMapMessage}

import akka.testkit.TestActorRef


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


  /*########################################################################
    Testing ListDatabaseMessage() receiving TU13
    ########################################################################*/


/*
  it should "actually return correct maplist when receiving a ListMapMessage" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef=TestActorRef(new Main(null))
    // retrieving the underlying actor
    val actor = actorRef.underlyingActor
    // putting extra maps in the storefinders map
    // now I send the message
    val future = actorRef ? ListDatabaseMessage()
    //when the message is completed i check that the StoremanagerActor reply correctly
    ScalaFutures.whenReady(future) {
      result => result should be(new ReplyMessage (EnumReplyResult.Done,new ListDatabaseMessage(),ListDBInfo(List[String]("master","test"))))
    }
  }
*/
  /*########################################################################
    Testing SelectDatabaseMessage() receiving TU14
    ########################################################################*/

  /*########################################################################
    Testing CreateDatabaseMessage() receiving TU15
    ########################################################################*/

  /*########################################################################
    Testing DeleteDatabaseMessage() receiving TU16
    ########################################################################*/

  /*########################################################################
    Testing SelectMapMessage() receiving TU17
    ########################################################################*/
}




