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

package server.enums

/**
  * This object contains the various actors properties.
  */
object EnumActorsProperties {

  val actorProperties = Seq(NumberOfNinjas, NumberOfWarehouseman, MaxStorefinderNumber, MaxStorekeeperNumber)

  /**
    * Represents an actor property
    */
  sealed trait ActorProperties

  /**
    * Represents the number of Ninja actor for each Storekeeper actor
    *
    * @see Storekeeper
    */
  case object NumberOfNinjas extends ActorProperties

  /**
    * Represents the number of Warehouseman actor for each Storekeeper actor
    *
    * @see Warehouseman
    * @see Storekeeper
    */
  case object NumberOfWarehouseman extends ActorProperties

  /**
    * Represents the max number of Storekeeper actor for each Storefinder actor
    *
    * @see Storekeeper
    * @see Storefinder
    */
  case object MaxStorekeeperNumber extends ActorProperties

  /**
    * Represents the max number of Storefinder actor for each Main actor
    *
    * @see Main
    * @see Storefinder
    */
  case object MaxStorefinderNumber extends ActorProperties

  /**
    * Represent the max number of Row that a Storekeeper actor can handle
    */
  case object MaxRowNumber extends ActorProperties

  /**
    * Represents the number of Ninja for each actor.
    */
  case object NinjaNumber extends ActorProperties

  /**
    * Represents the number of Warehouseman for each Storekeeper actor.
    */
  case object WarehousemanNumber extends ActorProperties

}
