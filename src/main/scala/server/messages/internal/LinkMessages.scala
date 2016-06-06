package server.messages.internal

import akka.actor.ActorRef

/**
  * Created by lucan on 11/05/2016.
  */

/**
  * LinkMessages are used to manage links between actors. These messages are used by actors to manage Actorbase
  * structure, they are not directly related to user requests.
  */
object LinkMessages {

  /**
    * Trait that every message that belongs to link operations has to extend.
    */
  trait LinkMessage

  /**
    * An AddNinjaMessage is used by a Storefinder to require the addition of a Ninja actor.
    * @param ref The reference to the actor.
    *
    * @see LinkMessage
    */
  case class AddNinjaMessage(ref : ActorRef) extends LinkMessage

  /**
    * An AddWarehousemanMessage is used by a Storefinder to require the addition of a Warehouseman actor.
    * @param ref The reference to the actor.
    *
    * @see LinkMessage
    */
  case class AddWarehousemanMessage(ref : ActorRef) extends LinkMessage

  /**
    * A RemoveNinjaMessage is used by a Storefinder to require the removal of a Ninja actor.
    * @param ref The reference to the actor.
    *
    * @see LinkMessage
    */
  case class RemoveNinjaMessage(ref : ActorRef) extends LinkMessage

  /**
    * A RemoveWarehousemanMessage is used by a Storefinder to require the removal of a Warehouseman actor.
    * @param ref The reference to the actor.
    *
    * @see LinkMessage
    */
  case class RemoveWarehousemanMessage(ref : ActorRef) extends LinkMessage

  /**
    * A BecomeStorefinderNinjaMessage is used by a Usermanager who has just become a Storefinder to tell his ninjas
    * to become StorefinderNinjas
    * @param c An array containing all the references to children
    * @param i An array containing all the indexes of children
    * @param n An array containing all the ninjas of children
    *
    * @see LinkMessage
    */
  case class BecomeStorefinderNinjaMessage(c : Array[ActorRef], i : Array[(String, String)], n : Array[Array[ActorRef]]) extends LinkMessage

}
