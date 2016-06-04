package server.messages.internal

import scala.collection.mutable

/**
  * Created by borto on 31/05/2016.
  *
  * ScalabilityMessages are used to manage the scaling properties of Actorbase.
  */
object ScalabilityMessages {

  /**
    * Trait that every message that belongs to scalability operations has to extend.
    */
  trait ScalabilityMessage

  /**
    * A SendMapMessage is used to tell an actor to transfer an amount of data to an actor during a scaling operation.
    *
    * @param map The data to be transferred
    * @see ScalabilityMessage
    */
  case class SendMapMessage (map: mutable.HashMap[String, Array[Byte]]) extends ScalabilityMessage

  /**
    * Set the number of Ninja actors.
    *
    * @param number The number of actors.
    * @see ScalabilityMessage
    */
  case class SetNinjaMessage (number : Integer) extends ScalabilityMessage

  /**
    * Set the max number of Storekeeper actors.
    *
    * @param number The number of actors.
    * @see ScalabilityMessage
    */
  case class MaxStorekeeperMessage (number : Integer) extends ScalabilityMessage

  /**
    * Set the max number of Storefinder actors.
    *
    * @param number The number of actors.
    * @see ScalabilityMessage
    */
  case class MaxStorefinderMessage (number : Integer) extends ScalabilityMessage

  /**
    * Set the number of Warehouseman actors.
    *
    * @param number The number of actors.
    * @see ScalabilityMessage
    */
  case class SetWarehousemanMessage (number : Integer) extends ScalabilityMessage

  /**
    * Set the max number of rows which can be contained in a single Storekeeper map.
    *
    * @param number The number of rows.
    * @see ScalabilityMessage
    */
  case class MaxRowsMessage (number : Integer) extends ScalabilityMessage
}
