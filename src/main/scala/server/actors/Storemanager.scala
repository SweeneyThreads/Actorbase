package server.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, Props}
import server.enums.EnumReplyResult
import server.enums.EnumReplyResult.{Done, Error}
import server.enums.EnumStoremanagerType._
import server.messages.query.ReplyMessage
import server.messages.query.user.RowMessages._

import scala.collection.JavaConversions._
import scala.language.postfixOps
import scala.util.{Failure, Success}
import akka.pattern.ask
import server.StaticSettings
import server.messages.internal.LinkMessages.{BecomeStorefinderNinjaMessage, LinkMessage}


/**
  * Created by matteobortolazzo on 03/06/2016.
  */

/**
  * A Storemanager manages data stored in RAM
  * @param data the data to manage
  * @param index the index of data
  * @param storemanagerType the behaviour of the storemanager
  * @param n the ninjas of the storemanager
  */
class Storemanager(data: ConcurrentHashMap[String,  Array[Byte]],index: (String, String), storemanagerType: StoremanagerType, n: Array[ActorRef] = null)
  extends ReplyActor {

  /**
    * Class that represents a child of a storemanager. A parent must have references of his children ref, index and
    * ninjas (to do the replacement if the main child deads)
    * @param a The child ActorRef
    * @param i The Child index
    * @param n The Child array of ninjas
    */
  class Child(a: ActorRef, i: (String, String), n: Array[ActorRef]) {
    val actor = a
    val index = i
    val ninjas = n
  }

  val map = data
  val children = new Array[Child](2)
  val ninjas = n


  if(map.keySet().size() >= StaticSettings.maxRowNumber)
    divideActor()

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
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
  }

  /**
    * Processes RowMessage messages.
    * Handles ListKeysMessage messages returning the list of keys in the actor.
    * Handles InsertRowMessage messages adding an entry in the map.
    * Handles UpdateRowMessage messages updating an entry in the map.
    * Handles RemoveRowMessage messages removing an entry with the given key.
    * Handles FindRowMessage messages returning the value of an entry with the given key.
    *
    * @param message The RowMessage message to precess.
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
          if(map.keySet().size() >= StaticSettings.maxRowNumber)
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
        // Create the list of key
        var keys = List[String]()
        // For each key, add to the list
        for (k: String <- map.keys()) keys = keys.::(k)
        // Reply with the list
        reply(ReplyMessage(EnumReplyResult.Done,message,ListKeyInfo(keys)))
      }
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
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
    for (i <- 0 to ninjas1.length) {
      ninjas1(i) = context.actorOf(Props(new Storemanager(map1, index1, StorekeeperNinjaType)))
    }
    val ninjas2 = new Array[ActorRef](StaticSettings.ninjaNumber)
    for (i <- 0 to ninjas2.length) {
      ninjas2(i) = context.actorOf(Props(new Storemanager(map2, index2, StorekeeperNinjaType)))
    }
    // Creates two children with the created maps and pass the ninjas to them
    val actor1 = context.actorOf(Props(new Storemanager(map1, index1, StorekeeperType, ninjas1)))
    val actor2 = context.actorOf(Props(new Storemanager(map2, index2, StorekeeperType, ninjas2)))
    // Adds children
    children(0) = new Child(actor1, index1, ninjas1)
    children(1) = new Child(actor2, index2, ninjas2)
    // Erase the map
    map.clear()
    log.info("Storekeeper split")
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
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, currentMethodName()))
  }

  /**
    * Processes RowMessage messages.
    * Handles ListKeysMessage messages asking to every Storekeeper actor the list of keys
    * and returning the complete list.
    * All other RowMessage messages are sent to the right Storekeeper actor.
    *
    * @param message The RowMessage message to precess.
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
      case ListKeysMessage() => {
        // save the original sender
        val origSender = sender
        // messages received back from storekeepers, initially 0
        var messagesReceived = 0
        //  list used to save all the keys of the map. initially Empty
        var keys = List[String]()
        // for every storekeeper in 'storekeeper' map
        for (i <- 0 to children.length) {
          val sk = children(i).actor
          // send the message to the storekeeper and save the reply in a Future
          val future = sk ? message
          // when the reply arrives
          future.onComplete {
            // if it's successful
            case Success(result) => {
              // increment the number of messages returned
              messagesReceived = messagesReceived + 1
              // the reply of a storekeeper contains the list of his keys(as a ListKeyInfo) in info field.
              val ris = result.asInstanceOf[ReplyMessage].info.asInstanceOf[ListKeyInfo].keys
              // if this partial list is not empty it's added to the main list
              if(ris.nonEmpty) keys = keys ::: ris
              // if messages received back is equal to the number of storekeeper the main list should be complete
              if (messagesReceived == children.length) {
                // if the main list is empty the reply to the sender is an "Error"
                if (keys.isEmpty) reply(ReplyMessage(Error, message, NoKeyInfo()), origSender)
                // if the main list is not empty the reply to the sender is "Done" and the list id returned as a ListKeyInfo
                // keys list can be unsorted, cause the order of storekeeper's response is unknown. So keys is sorted before the reply
                else reply(ReplyMessage(Done, message, ListKeyInfo(keys.sorted)), origSender)
              }
            }
            // NB: the method returns the keys he manage to get. If a Failure occurs it is logged, but the user is not informed.
            case Failure(t) => {
              // messages are incremented to complete the loop
              messagesReceived = messagesReceived + 1
              // the error is logged
              log.error("Error sending message: " + t.getMessage)
              // if messages received back is equal to the number of storekeeper the main list should be complete
              if (messagesReceived == children.length) {
                // if the main list is empty the reply to the sender is an "Error"
                if (keys.isEmpty) reply(ReplyMessage(Error, message, NoKeyInfo()), origSender)
                // if the main list is not empty the reply to the sender is "Done" and the list id returned as a ListKeyInfo
                // keys list can be unsorted, cause the order of storekeeper's response is unknown. So keys is sorted before the reply
                else reply(ReplyMessage(Done, message, ListKeyInfo(keys.sorted)), origSender)
              }
            }
          }
        }
      }
      // if the message type is InsertRowMessage, forward it to the storekeeper
      case InsertRowMessage(key: String, value: Array[Byte]) => sendToStorekeeper(key, message)
      // if the message type is UpdateRowMessage, forward it to the storekeeper
      case UpdateRowMessage(key: String, value: Array[Byte]) => sendToStorekeeper(key, message)
      // if the message type is RemoveRowMessage, forward it to the storekeeper
      case RemoveRowMessage(key: String) => sendToStorekeeper(key, message)
      // if the message type is FindRowMessage, forward it to the storekeeper
      case FindRowMessage(key: String) => sendToStorekeeper(key, message)
      // if the message type is not a RowMessage, log an error
      case _ => log.error(replyBuilder.unhandledMessage(self.path.toString,currentMethodName()))
    }
  }

  /**
    * Sends the message to the right Storekeeper.
    *
    * @param key The key to user.
    * @param message The message to send.
    */
  private def sendToStorekeeper(key: String, message: RowMessage): Unit = {
    val sk = findRightStorekeeper(key)
    // save the original sender
    val origSender = sender
    // send the message to the storekeeper and save the reply in a Future
    for (i <- 0 to sk.ninjas.length) {
      sk.ninjas(i) ! message
    }
    val future = sk.actor ? message
    future.onComplete {
      // reply the storemanager with the reply from the storekeeper
      case Success(result) => reply(result.asInstanceOf[ReplyMessage], origSender)
      case Failure(t) => log.error("Error sending message: " + t.getMessage)
    }
  }

  /**
    * Returns the child that handles the key.
    *
    * @param key The key to use.
    * @return The Storekeeper actor reference.
    */
  private def findRightStorekeeper(key:String): Child = {
    val firstChild = children(0)
    val secondChild = children(1)
    if(key <= firstChild.index._2)
      return firstChild
    return secondChild
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
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, currentMethodName()))
  }

  /**
    * Processes RowMessage messages.
    * Handles InsertRowMessage messages adding an entry in the map.
    * Handles UpdateRowMessage messages updating an entry in the map.
    * Handles RemoveRowMessage messages removing an entry with the given key.
    *
    * @param message The RowMessage message to precess.
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
        writeLog(ReplyMessage(EnumReplyResult.Done,message))
      }
      // If the storemanager send an update message
      case UpdateRowMessage(key: String, value: Array[Byte]) => {
        // If the key doesn't exist
        if (map.containsKey(key)) return
        // If the key exists
        map.put(key, value)
        writeLog(ReplyMessage(EnumReplyResult.Done,message))
      }
      // If the storemanager send a remove message
      case RemoveRowMessage(key: String) => {
        if (!map.containsKey(key)) return
        map.remove(key)
        writeLog(ReplyMessage(EnumReplyResult.Done,message))
      }
      case _ => return
    }
  }

  private def handleLinkMessagesAsStorekeeperNinja(message: LinkMessage): Unit = {
    message match {
      case BecomeStorefinderNinjaMessage(c : Array[ActorRef], i : Array[(String, String)], n : Array[Array[ActorRef]]) => {
        context.become(receiveAsStorefinderNinja)
        map.clear()
        children(0) = new Child(c(0), i(0), n(0))
        children(1) = new Child(c(1), i(1), n(1))
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
    case other => log.error(replyBuilder.unhandledMessage(self.path.toString, currentMethodName()))
  }

  /**
    * Processes RowMessage messages as StorefinderNinja.
    * Because a parent doesn't know his children behaviour he will always forward all his messages to his children's
    * ninjas, therefore even if a StorefinderNinja is simply a copy of the Storefinder it will receive all the
    * RowMessages and has to handle them without error.
    *
    * @param message The RowMessage message to precess.
    * @see RowMessage
    * @see ReplyMessage
    */
  private def handleRowMessagesAsStorefinderNinja(message: RowMessage): Unit = {
    message match {
      case _ => return
    }
  }
}
