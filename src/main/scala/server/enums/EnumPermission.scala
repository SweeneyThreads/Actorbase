package server.enums

/** Permission types */
object EnumPermission {
  val permissionsType = Seq(Read, ReadWrite)
  sealed trait UserPermission
  case object Read extends UserPermission
  case object ReadWrite extends UserPermission
}
