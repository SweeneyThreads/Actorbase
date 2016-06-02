package server.enums

/**
  * Created by matteobortolazzo on 02/05/2016.
  * Contains permissions type classes
  */
object EnumPermission {
  val permissionsType = Seq(Read, ReadWrite)

  /**
    * Represents an user permission
    */
  sealed trait UserPermission

  /**
    * Represents an user read permission
    */
  case object Read extends UserPermission

  /**
    * Represents an user write permission
    */
  case object ReadWrite extends UserPermission
}
