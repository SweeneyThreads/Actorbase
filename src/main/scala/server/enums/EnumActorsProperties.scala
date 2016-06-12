package server.enums

/**
  * Created by lucan on 02/06/2016.
  * contains the various actors properties
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
