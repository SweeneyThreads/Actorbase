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

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, Props}
import server.enums.EnumReplyResult
import server.enums.EnumReplyResult.{Done, Error}
import server.enums.EnumStoremanagerType._
import server.messages.query.{ReplyMessage, ServiceErrorInfo}
import server.messages.query.user.RowMessages._

import scala.collection.JavaConversions._
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}
import server.StaticSettings
import server.messages.internal.LinkMessages.{BecomeStorefinderNinjaMessage, LinkMessage}
import akka.pattern.ask

/**
  * A Storemanager manages data stored in RAM
  *
  * @param map the data to manage
  * @param index the index of data
  * @param storemanagerType the behaviour of the storemanager
  * @param ninjas the ninjas of the storemanager
  */
class Storemanager(var map: ConcurrentHashMap[String,  Array[Byte]],
    val index: (String, String),
    val storemanagerType: StoremanagerType,
    val ninjas: Array[ActorRef] = null)
  extends ReplyActor {

  /**
    * Class that represents a child of a storemanager. A parent must have references of his children ref, index and
    * ninjas (to do the replacement if the main child deads)
    *
    * @param a The child ActorRef
    * @param i The Child index
    * @param n The Child array of ninjas
    */
  class Child(a: ActorRef, i: (String, String), n: Array[ActorRef]) {
    val actor = a
    val index = i
    val ninjas = n
  }

  var leftChild: Child = null
  var rightChild: Child = null


  if (map.keySet().size() > StaticSettings.maxRowNumber) {
    log.info("Trying to create a Storemanager with too big map, Storemanager splitted automatically")
    divideActor()
  }



  /**
    * Override of the actor's preStart method.
    * Changes the actor's behaviour based on the constructor.
    *
    * @see #become(Actor.receive)
    */
  override def preStart(): Unit = {
    storemanagerType match {
      // Changes the actor's behaviour to the Storefinder actor's one.
      case StorefinderType => context.become(receiveAsStoreFinder)
      // Changes the actor's behaviour to the StorekeeperNinja actor's one.
      case StorekeeperNinjaType => context.become(receiveAsStorekeeperNinja)
      // Changes the actor's behaviour to the StorefinderNinja actor's one.
      case StorefinderNinjaType => context.become(receiveAsStorefinderNinja)
      case _ =>
    }
  }

  // Storekeeper behaviour

  /**
    * Processes all incoming messages behaving like a Storekeeper actor.
    * It handles only RowMessage messages
    *
    * @see #handleRowMessageAsStorefinder(RowMessage)
    */
  def receive = {
    // StoreFinder should receive and handle only RowMessages
    case m:RowMessage => handleRowMessageAsStorekeeper(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString,"receive"))
  }

  /**
    * Processes RowMessage messages.
    * Handles ListKeysMessage messages returning the list of keys in the actor.
    * Handles InsertRowMessage messages adding an entry in the map.
    * Handles UpdateRowMessage messages updating an entry in the map.
    * Handles RemoveRowMessage messages removing an entry with the given key.
    * Handles FindRowMessage messages returning the value of an entry with the given key.
    *
    * @param message The RowMessage message to process.
    * @see RowMessage
    * @see ReplyMessage
    */
  private def handleRowMessageAsStorekeeper(message: RowMessage): Unit = {
    message match {
      // If the user types "insert '<key>' <value>"
      case InsertRowMessage(key: String, value: Array[Byte]) => {
        println(sender())
        // If the storekeeper already contains that key
        if (map.containsKey(key)) reply(ReplyMessage(EnumReplyResult.Error,message,KeyAlreadyExistInfo()))
        // If the storekeeper doesn't have that key
        else {
          // Insert the entry
          map.put(key, value)
          logAndReply(ReplyMessage(EnumReplyResult.Done, message))
          if(map.keySet().size() > StaticSettings.maxRowNumber)
            divideActor()
        }
      }
      // If the user types "udpdate '<key>' <value>"
      case UpdateRowMessage(key: String, value: Array[Byte]) => {
        // If the storekeeper doesn't have that key
        if (!map.containsKey(key)) reply(ReplyMessage(EnumReplyResult.Error,message,KeyDoesNotExistInfo()))
        // If the storekeeper contains that key
        else {
          // Update the entry
          map.put(key, value)
          logAndReply(ReplyMessage(Done,message))
        }
      }
      // If the user types "remove '<key>'"
      case RemoveRowMessage(key: String) => {
        // If the storekeeper doesn't have that key
        if (!map.containsKey(key)) reply(ReplyMessage(EnumReplyResult.Error,message,KeyDoesNotExistInfo()))
        // If the storekeeper contains that key
        else {
          // Remove the entry
          map.remove(key)
          logAndReply(ReplyMessage(Done,message))
        }
      }
      // If the user types "find '<key>'"
      case FindRowMessage(key: String) => {
        // If the storekeeper doesn't have that key
        if (!map.containsKey(key)) reply(ReplyMessage(EnumReplyResult.Error,message,KeyDoesNotExistInfo()))
        // If the storekeeper contains that key
        else reply(ReplyMessage(EnumReplyResult.Done,message, FindInfo(map.get(key))))
      }
      // If the user types "listkey"
      case ListKeysMessage() => {
        if (map.isEmpty) reply(ReplyMessage(EnumReplyResult.Error, message, NoKeyInfo()))
        // Create the list of key
        var keys = List[String]()
        // For each key, add to the list
        for (k: String <- map.keys()) keys = keys.::(k)
        // Reply with the list
        reply(ReplyMessage(EnumReplyResult.Done,message,ListKeyInfo(keys)))
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString, "handleRowMessageAsStorekeeper"))
    }
  }

  /**
    * It changes the actor's behaviour to Storefinders,
    * it creates two Storekeeper children and sends half of the map each.
    */
  private def divideActor() : Unit = {
    context.become(receiveAsStoreFinder)
    // Creates maps to pass at children
    val map1 = new ConcurrentHashMap[String, Array[Byte]]()
    val map2 = new ConcurrentHashMap[String, Array[Byte]]()
    // Fills the two maps and finds the mid element
    var midElement = ""
    var i = 0
    for(key <- map.keySet()) {
      if(i < map.keySet().size() / 2) {
        map1.put(key, map.get(key))
        midElement = key
      }
      else
        map2.put(key, map.get(key))
      i = i + 1
    }
    // Creates new indexes
    val index1 = (index._1, midElement)
    val index2 = (midElement, index._2)
    // Creates two ninjas array
    val ninjas1 = new Array[ActorRef](StaticSettings.ninjaNumber)
    for (i <- ninjas1.indices) {
      ninjas1(i) = context.actorOf(Props(new Storemanager(new ConcurrentHashMap[String, Array[Byte]](map1), index1, StorekeeperNinjaType)))
    }
    val ninjas2 = new Array[ActorRef](StaticSettings.ninjaNumber)
    for (i <- ninjas2.indices) {
      ninjas2(i) = context.actorOf(Props(new Storemanager(new ConcurrentHashMap[String, Array[Byte]](map2), index2, StorekeeperNinjaType)))
    }
    // Creates two children with the created maps and pass the ninjas to them
    val actor1 = context.actorOf(Props(new Storemanager(map1, index1, StorekeeperType, ninjas1)))
    val actor2 = context.actorOf(Props(new Storemanager(map2, index2, StorekeeperType, ninjas2)))
    // Adds children
    leftChild = new Child(actor1, index1, ninjas1)
    rightChild = new Child(actor2, index2, ninjas2)
    // Erase the map
    map.clear()
    log.info("Storekeeper splitted")
  }

  // Storefinder behaviour

  /**
    * Processes all incoming messages behaving like a Storefinder actor.
    * It handles only RowMessage messages.
    *
    * @see RowLevelMessage
    * @see #handleRowMessageAsStorefinder(RowLevelMessage)
    */
  private def receiveAsStoreFinder: Receive = {
    // If it's a row level message
    case m: RowMessage => handleRowMessageAsStorefinder(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, "receiveAsStoreFinder"))
  }

  /**
    * Handles a ListKeyMessage as a storefinder (normal or ninja). It sends the message to his children, first the left
    * child and second the right one. It randomizes the selection of the main child actor or one of his ninjas.
    *
    * @param message
    */
  private def handleKeysMessageAsStorefinder(message: RowMessage) : Unit = {
    // save the original sender
    val origSender = sender
    //  list used to save all the keys of the map. initially Empty
    var keys = List[String]()

    // Generate a random Int between 0 and the number of leftChild's ninjas + 1
    val random = new Random()
    val randomNumber = random.nextInt(leftChild.ninjas.length + 1)
    // Select the actor to send the message based on the random number
    var leftActor: ActorRef = null
    if (randomNumber < leftChild.ninjas.length) {
      leftActor = leftChild.ninjas(randomNumber)
    }
    else {
      leftActor = leftChild.actor
    }

    val leftFuture = leftActor ? message

    // When the left branch has completed the requests save results and ask the right branch
    leftFuture.onComplete {
      case Success(result1) => {
        val res1 = result1.asInstanceOf[ReplyMessage].info.asInstanceOf[ListKeyInfo].keys
        if (res1.nonEmpty) keys = keys ::: res1

        // Generate a random Int between 0 and the number of leftChild's ninjas + 1
        val random = new Random()
        val randomNumber = random.nextInt(rightChild.ninjas.length + 1)
        // Select the actor to send the message based on the random number
        var rightActor: ActorRef = null
        if (randomNumber < rightChild.ninjas.length) {
          rightActor = rightChild.ninjas(randomNumber)
        }
        else {
          rightActor = rightChild.actor
        }

        val rightFuture = rightActor ? message
        // When the right branch has completed merge results
        rightFuture.onComplete {
          case Success(result2) => {
            val res2 = result2.asInstanceOf[ReplyMessage].info.asInstanceOf[ListKeyInfo].keys
            if (res2.nonEmpty) keys = keys ::: res2

            if (keys.isEmpty) reply(ReplyMessage(Error, message, NoKeyInfo()), origSender)

            else reply(ReplyMessage(Done, message, ListKeyInfo(keys.sorted)), origSender)
          }
          case Failure(t2) => {
            log.error("Error sending message: " + t2.getMessage)
            reply(new ReplyMessage(EnumReplyResult.Error, message,
              new ServiceErrorInfo("Error sending message: " + t2.getMessage)), origSender)
          }
        }
      }
      case Failure(t1) => {
        log.error("Error sending message: " + t1.getMessage)
        reply(new ReplyMessage(EnumReplyResult.Error, message,
          new ServiceErrorInfo("Error sending message: " + t1.getMessage)), origSender)
      }
    }
  }

  /**
    * Processes RowMessage messages.
    * Handles ListKeysMessage messages asking to every Storekeeper actor the list of keys
    * and returning the complete list.
    * All other RowMessage messages are sent to the right Storekeeper actor.
    *
    * @param message The RowMessage message to process.
    * @see #sendToStorekeeper(String, RowMessage)
    * @see RowMessage
    * @see ListKeysMessage
    * @see InsertRowMessage
    * @see UpdateRowMessage
    * @see FindRowMessage
    * @see Storekeeper
    */
  private def handleRowMessageAsStorefinder(message: RowMessage) : Unit = {
    message match {
      // if the user types 'keys'
      case ListKeysMessage() => handleKeysMessageAsStorefinder(message)
      // if the message type is InsertRowMessage, forward it to the storekeeper
      case InsertRowMessage(key: String, value: Array[Byte]) => sendToStorekeeper(key, message)
      // if the message type is UpdateRowMessage, forward it to the storekeeper
      case UpdateRowMessage(key: String, value: Array[Byte]) => sendToStorekeeper(key, message)
      // if the message type is RemoveRowMessage, forward it to the storekeeper
      case RemoveRowMessage(key: String) => sendToStorekeeper(key, message)
      // if the message type is FindRowMessage, forward it to the storekeeper
      case FindRowMessage(key: String) => sendToStorekeeper(key, message)
      // if the message type is not a RowMessage, log an error
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,"handleRowMessageAsStorefinder"))
    }
  }

  /**
    * Sends the message to the right Storekeeper. If it's a FindRowMessage the method selects one random child between
    * the correct child and his ninjas.
    *
    * @param key The key to user.
    * @param message The message to send.
    */
  private def sendToStorekeeper(key: String, message: RowMessage): Unit = {
    val sk = findRightStorekeeper(key)
    message match {
      // If the message is a FindRowMessage send it to a random actor between the child and his ninjas
      case FindRowMessage(key: String) => {
        // Generate a random Int between 0 and the number of child's ninjas + 1
        val random = new Random()
        val randomNumber = random.nextInt(sk.ninjas.length + 1)
        // Select the actor to send the message based on the random number
        if (randomNumber < sk.ninjas.length) {
          sk.ninjas(randomNumber) forward message
        }
        else {
          sk.actor forward message
        }
      }
      case _ => {
        // send the message to the storekeeper and save the reply in a Future
        for (i <- sk.ninjas.indices ) {
          sk.ninjas(i) ! message
        }
        // Forewards the message to the storekeeper
        sk.actor forward message
      }
    }
  }

  /**
    * Returns the child that handles the key.
    *
    * @param key The key to use.
    * @return The Storekeeper actor reference.
    */
  private def findRightStorekeeper(key:String): Child = {
    if(key <= leftChild.index._2)
      return leftChild
    return rightChild
  }

  // StorekeeperNinja behaviour
  /**
    * Processes all incoming messages behaving like a Ninja actor.
    * It handles only RowMessage messages.
    *
    * @see RowLevelMessage
    * @see #handleRowMessagesAsNinja(RowLevelMessage)
    */
  private def receiveAsStorekeeperNinja: Receive = {
    // If it's a row level message
    case m: RowMessage => handleRowMessagesAsStorekeeperNinja(m)
    case m: LinkMessage => handleLinkMessagesAsStorekeeperNinja(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, "receiveAsStorekeeperNinja"))
  }

  /**
    * Processes RowMessage messages.
    * Handles InsertRowMessage messages adding an entry in the map.
    * Handles UpdateRowMessage messages updating an entry in the map.
    * Handles RemoveRowMessage messages removing an entry with the given key.
    * Handles FindRowMessage message replying to the sender
    * Handles ListKeysMessage message replying to the sender
    *
    * @param message The RowMessage message to process.
    * @see RowMessage
    * @see ReplyMessage
    */
  private def handleRowMessagesAsStorekeeperNinja(message: RowMessage): Unit = {
    message match {
      // If the storemanager send an insert message
      case InsertRowMessage(key: String, value: Array[Byte]) => {
        // If the key already exists
        if (map.containsKey(key)) return
        // If the key doesn't exist
        map.put(key, value)
      }
      // If the storemanager send an update message
      case UpdateRowMessage(key: String, value: Array[Byte]) => {
        // If the key doesn't exist
        if (!map.containsKey(key)) return
        // If the key exists
        map.put(key, value)
      }
      // If the storemanager send a remove message
      case RemoveRowMessage(key: String) => {
        if (!map.containsKey(key)) return
        map.remove(key)
      }
      // If the user types "find '<key>'"
      case FindRowMessage(key: String) => {
        // If the storekeeper doesn't have that key
        if (!map.containsKey(key)) reply(ReplyMessage(EnumReplyResult.Error,message,KeyDoesNotExistInfo()))
        // If the storekeeper contains that key
        else reply(ReplyMessage(EnumReplyResult.Done,message, FindInfo(map.get(key))))
      }
      // If the user types "listkey"
      case ListKeysMessage() => {
        if (map.isEmpty) reply(ReplyMessage(EnumReplyResult.Error, message, NoKeyInfo()))
        // Create the list of key
        var keys = List[String]()
        // For each key, add to the list
        for (k: String <- map.keys()) keys = keys.::(k)
        // Reply with the list
        reply(ReplyMessage(EnumReplyResult.Done,message,ListKeyInfo(keys)))
      }
      case _ => return
    }
  }

  private def handleLinkMessagesAsStorekeeperNinja(message: LinkMessage): Unit = {
    message match {
      case BecomeStorefinderNinjaMessage(c : Array[ActorRef], i : Array[(String, String)], n : Array[Array[ActorRef]]) => {
        context.become(receiveAsStorefinderNinja)
        map.clear()
        leftChild = new Child(c(0), i(0), n(0))
        rightChild = new Child(c(1), i(1), n(1))
      }
    }
  }

  // StorefinderNinja behaviour

  /**
    * Processes all incoming messages behaving like a Ninja actor.
    * It handles only RowMessage messages.
    *
    * @see RowLevelMessage
    * @see #handleRowMessagesAsNinja(RowLevelMessage)
    */
  private def receiveAsStorefinderNinja: Receive = {
    // If it's a row level message
    case m: RowMessage => handleRowMessagesAsStorefinderNinja(m)
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, "receiveAsStorefinderNinja"))
  }

  /**
    * Processes RowMessage messages as StorefinderNinja.
    *
    * @param message The RowMessage message to process.
    * @see RowMessage
    * @see ReplyMessage
    */
  private def handleRowMessagesAsStorefinderNinja(message: RowMessage): Unit = {
    message match {
      // if the message type is FindRowMessage, forward it to the storekeeper
      case FindRowMessage(key: String) => {
        log.info("hello i'm a storefinder message and i'm handling a findrowmessage")
        sendToStorekeeper(key, message)
      }
      case ListKeysMessage() => handleKeysMessageAsStorefinder(message)
      case _ => return
    }
  }
}
