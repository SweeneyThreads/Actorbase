package server.enums

/**
  * Created by kurt on 15/06/2016.
  */
object EnumWarehousemanType {

  trait WarehousemanType

  case object RowWarehousemanType extends WarehousemanType

  case object MapWarehousemanType extends WarehousemanType

  case object DatabaseWarehousemanType extends WarehousemanType
}
