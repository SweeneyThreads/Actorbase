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

import java.util
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, OneForOneStrategy, Props, Terminated}
import server.enums.EnumReplyResult
import server.enums.EnumReplyResult.{Done, Error}
import server.enums.EnumStoremanagerType._
import server.messages.query.{ReplyErrorInfo, ReplyMessage}
import server.messages.query.user.RowMessages._

import scala.language.postfixOps
import scala.util.{Failure, Random, Success}
import server.StaticSettings
import server.messages.internal.StoremanagerMessages.{BecomeMainStoremanager, BecomeStorefinderNinjaMessage, StoremanagerMessage}
import akka.pattern.ask

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.collection.JavaConversions._
import scala.concurrent.duration._


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
                   var ninjas: util.ArrayList[ActorRef] = null)
  extends ReplyActor {

  /**
    * Class that represents a child of a storemanager. A parent must have references of his children ref, index and
    * ninjas (to do the replacement if the main child deads)
    *
    * @param a The child ActorRef
    * @param i The Child index
    * @param n The Child array of ninjas
    */
  class Child(a: ActorRef, i: (String, String), n: util.ArrayList[ActorRef]) {
    var actor = a
    val index = i
    var ninjas = n
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
    * @see #handleRowMessageAsStorekeeper(RowMessage)
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
        if (!map.containsKey(key))
          reply(ReplyMessage(EnumReplyResult.Error, message, KeyDoesNotExistInfo()))
        // If the storekeeper contains that key
        else {
          // Remove the entry
          map.remove(key)
          logAndReply(ReplyMessage(Done, message))
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
        if (map.isEmpty) reply(ReplyMessage(EnumReplyResult.Error, message, NoKeysInfo()))
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
    val orderedKeys = new util.ArrayList(map.keySet())
    Collections.sort(orderedKeys)
    // Creates maps to pass at children
    val map1 = new ConcurrentHashMap[String, Array[Byte]]()
    val map2 = new ConcurrentHashMap[String, Array[Byte]]()
    // Fills the two maps and finds the mid element
    var midElement = ""
    var i = 0
    for(key <- orderedKeys) {
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
    val ninjas1 = new util.ArrayList[ActorRef](StaticSettings.ninjaNumber)
    for (i <- ninjas1.indices) {
      ninjas1(i) = context.actorOf(Props(new Storemanager(new ConcurrentHashMap[String, Array[Byte]](map1), index1, StorekeeperNinjaType)))
    }
    val ninjas2 = new util.ArrayList[ActorRef](StaticSettings.ninjaNumber)
    for (i <- ninjas2.indices) {
      ninjas2(i) = context.actorOf(Props(new Storemanager(new ConcurrentHashMap[String, Array[Byte]](map2), index2, StorekeeperNinjaType)))
    }
    // Creates two children with the created maps and pass the ninjas to them
    val leftActor = context.actorOf(Props(new Storemanager(map1, index1, StorekeeperType, ninjas1)))
    val rightActor = context.actorOf(Props(new Storemanager(map2, index2, StorekeeperType, ninjas2)))
    // Watches for actors death
    context.watch(leftActor)
    context.watch(rightActor)
    // Adds children
    leftChild = new Child(leftActor, index1, ninjas1)
    rightChild = new Child(rightActor, index2, ninjas2)
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
    case Terminated(actor: ActorRef) => handleDeadChild(actor)
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
    // Choose a left child randomly
    var leftActor: ActorRef = null
    if (randomNumber < leftChild.ninjas.length) leftActor = leftChild.ninjas(randomNumber)
    else leftActor = leftChild.actor
    // Choose a right child randomly
    var rightActor: ActorRef = null
    if (randomNumber < rightChild.ninjas.length) rightActor = rightChild.ninjas(randomNumber)
    else rightActor = rightChild.actor
    // Launch the two futures
    val futures = new ListBuffer[Future[ReplyMessage]]
    val leftFuture = leftActor ? message
    val rightFuture = rightActor ? message
    futures += leftFuture.asInstanceOf[Future[ReplyMessage]]
    futures += rightFuture.asInstanceOf[Future[ReplyMessage]]
    // Handles the left future
    leftFuture.onComplete {
      case Success(result1) => {
        val reply = result1.asInstanceOf[ReplyMessage]
        reply.result match {
          case Done => keys = keys ::: reply.info.asInstanceOf[ListKeyInfo].keys
          case Error =>
        }
      }
      case Failure(t) => reply(ReplyMessage(EnumReplyResult.Error, message, ReplyErrorInfo()))
    }
    // Handles the right future
    rightFuture.onComplete {
      case Success(result1) => {
        val reply = result1.asInstanceOf[ReplyMessage]
        reply.result match {
          case Done => keys = keys ::: reply.info.asInstanceOf[ListKeyInfo].keys
          case Error =>
        }
      }
      case Failure(t) => reply(ReplyMessage(EnumReplyResult.Error, message, ReplyErrorInfo()))
    }
    val f = Future.sequence(futures.toList)
    Await.ready(f, Duration.Inf)
    // If the key list is empty
    if(keys.isEmpty)
      reply(ReplyMessage(Error, message, new NoKeysInfo), origSender)
    reply(ReplyMessage(Done, message, new ListKeyInfo(keys.sorted)), origSender)
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

  /**
    * Replace the dead Storekeeper actor with one of its Ninja actors
    *
    * @param actor The dead child actor
    */
  private def handleDeadChild(actor: ActorRef): Unit  = {
    var child = leftChild
    if(actor == rightChild.actor)
      child = rightChild

    val firstNinja = child.ninjas(0)
    child.ninjas.drop(0)
    child.actor = firstNinja
    val future = child.actor ? BecomeMainStoremanager(child.ninjas)
    val newNinja = Await.result(future, 5 seconds).asInstanceOf[ActorRef]
    child.ninjas.add(newNinja)
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
    case m: StoremanagerMessage => handleNinjaMessagesAsStorekeeperNinja(m)
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
        if (map.isEmpty) reply(ReplyMessage(EnumReplyResult.Error, message, NoKeysInfo()))
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

  /**
    * Processes StoremanagerMessage messages as StorekeeperNinja.
    *
    * @param message The StoremanagerMessage message to process
    */
  private def handleNinjaMessagesAsStorekeeperNinja(message: StoremanagerMessage): Unit = {
    message match {
      case BecomeStorefinderNinjaMessage(c: Array[ActorRef], i: Array[(String, String)], n: Array[util.ArrayList[ActorRef]]) =>
        context.become(receiveAsStorefinderNinja)
        map.clear()
        leftChild = new Child(c(0), i(0), n(0))
        rightChild = new Child(c(1), i(1), n(1))
      case BecomeMainStoremanager(nin: util.ArrayList[ActorRef]) =>
        ninjas = nin
        // Change behaviour to Storekeeper
        context.become(receive)
        // Create a new Ninja to take its place
        val newNinja = context.actorOf(Props(new Storemanager(map, index, StorekeeperNinjaType)))
        // Add the new ninja to his list
        ninjas.add(newNinja)
        // Return the new ninja to the parent
        Some(sender).map(_ ! newNinja)
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
    case m: StoremanagerMessage => handleStoremanagerMessagesAsStorefinderNinja(m)
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

  /**
    * Processes StoremanagerMessage messages as StorefinderNinja.
    *
    * @param message The StoremanagerMessage message to process
    */
  private def handleStoremanagerMessagesAsStorefinderNinja(message: StoremanagerMessage): Unit = {
    message match {
      case BecomeMainStoremanager(nin: util.ArrayList[ActorRef]) =>
        ninjas = nin
        // Change behaviour to Storekeeper
        context.become(receiveAsStoreFinder)
        // Create a new Ninja to take its place
        val newNinja = context.actorOf(Props(new Storemanager(map, index, StorefinderNinjaType)))
        // Add the new ninja to his list
        ninjas.add(newNinja)
        // Return the new ninja to the parent
        Some(sender).map(_ ! newNinja)
    }
  }
}
