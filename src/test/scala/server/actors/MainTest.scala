
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




