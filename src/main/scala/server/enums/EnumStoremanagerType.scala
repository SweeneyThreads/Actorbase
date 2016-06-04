package server.enums

/**
  * Created by matteobortolazzo on 02/05/2016.
  * Contains permissions type classes
  */
object EnumStoremanagerType {
  val storemanagerTypes = Seq(StorefinderType, StorekeeperType, StorekeeperNinjaType)

  /**
    * Represents a Storemanager type
    */
  sealed trait StoremanagerType

  /**
    * Represents a Storefinder type
    */
  case object StorefinderType extends StoremanagerType

  /**
    * Represents a Storekeeper type
    */
  case object StorekeeperType extends StoremanagerType

  /**
    * Represents a StorekeeperNinja type
    */
  case object StorekeeperNinjaType extends StoremanagerType

  /**
    * Represents a StorefinderNinja type
    */
  case object StorefinderNinjaType extends StoremanagerType
}
