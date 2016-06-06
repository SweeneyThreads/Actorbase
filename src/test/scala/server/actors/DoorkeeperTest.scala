package server.actors

import java.net.InetSocketAddress
import java.text.SimpleDateFormat
import java.util.Calendar

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.io.Tcp.{Connected, Bound}
import akka.testkit.TestActorRef
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import server.enums.EnumReplyResult
import server.messages.query.ReplyMessage
import server.messages.query.user.DatabaseMessages._
import server.messages.query.user.RowMessages._

import scala.language.postfixOps

/**
  * Created by mattia on 03/6/2016.
  */
class DoorkeeperTest extends FlatSpec with Matchers with MockFactory {

  import akka.dispatch.ExecutionContexts._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._


  var System: ActorSystem = ActorSystem("System")
  var log: LoggingAdapter = Logging.getLogger(System, this)
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global
  //implicit val system = ActorSystem()

  /*########################################################################
    Testing doorkeeper correct log after bound message receiving TU11
    ########################################################################*/
  /*testing if the doorkeeper produces correct log after a bound message*/


  "DoorkeeperActor" should "create the correct log line when receiving a bound message" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = System.actorOf(Props(classOf[Doorkeeper],3131))
    // now I send the message
    actorRef ! Bound(new InetSocketAddress(3131))
    //take the time now
    val today = Calendar.getInstance.getTime
    // date format for year month and day
    val todayFormat = new SimpleDateFormat("yyyy-MM-dd")
    //create a String with today values
    val todayString: String = todayFormat.format(today)
    //get the today log file
    val source = scala.io.Source.fromFile("logs/actorbase." + todayString + ".0.log")
    //get the string of all the file and close the file
    val lines = try source.mkString finally source.close()
    //date format for hours minutes and seconds
    val nowFormat = new SimpleDateFormat("HH:mm:ss")
    //create a String with now time
    val nowString = nowFormat.format(today)
    //regular expression that match the info log produced by the doorkeeper
    val Pattern = (nowString + "....\\s.+\\sINFO\\s\\sserver\\.actors\\.Doorkeeper\\s\\-\\sPort\\s3131\\sopened").r
    //if it doesn't find the line the log didn't happened correctly
    Pattern.findFirstIn(lines) shouldNot be(None)
  }
  /*########################################################################
  Testing doorkeeper correct log after connected message receiving TU12
  ########################################################################*/
  /*testing if the doorkeeper produces correct log after a connected message*/


  "DoorkeeperActor" should "create the correct log line whan receinving a connected message" in {
    // TestActorRef is a exoteric function provided by akka-testkit
    // it creates a special actorRef that could be used for test purpose
    val actorRef = System.actorOf(Props(classOf[Doorkeeper],5151))
    // now I send the message
    actorRef ! Connected(new InetSocketAddress(2121),new InetSocketAddress(5151))
    // a take the time now
    val today = Calendar.getInstance.getTime
    // date format for year month and day
    val todayFormat = new SimpleDateFormat("yyyy-MM-dd")
    //create a String with today values
    val todayString: String = todayFormat.format(today)
    //sleep for ensuring that the doorkeeper has product the log
    Thread.sleep(100)
    //get the today log file
    val source = scala.io.Source.fromFile("logs/actorbase." + todayString + ".0.log")
    //get the string of all the file and close the file
    val lines = try source.mkString finally source.close()
    //date format for hours minutes and seconds
    val nowFormat = new SimpleDateFormat("HH:mm:ss")
    //create a String with now time
    val nowString = nowFormat.format(today)
    //regular expression that match the error log produced by the doorkeeper
    val Pattern = (nowString + ".+\\s.+\\sINFO\\s\\sserver\\.actors\\.Doorkeeper\\s\\-\\s0\\.0\\.0\\.0\\sconnected").r
    //if it doesn't find the line the log didn't happened correctly
    Pattern.findFirstIn(lines) shouldNot be(None)
  }
}