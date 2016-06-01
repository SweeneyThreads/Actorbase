package server.enums

/**
  * Created by matteobortolazzo on 02/05/2016.
  * Contains permissions type classes
  */
object EnumPermission {
  val permissionsType = Seq(Read, ReadWrite)

  /**
    * Represent an user permission
    */
  sealed trait UserPermission

  /**
    * Represent an user read permission
    */
  case object Read extends UserPermission

  /**
    * Represent an user write permission
    */
  case object ReadWrite extends UserPermission
}
