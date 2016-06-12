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

package server.messages.internal

import akka.actor.ActorRef


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
