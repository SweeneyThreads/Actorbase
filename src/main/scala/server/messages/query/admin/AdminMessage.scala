package server.messages.query.admin

import server.messages.query.PermissionMessages.AdminPermissionMessage
import server.messages.query.QueryMessage

/**
  * Created by lucan on 10/05/2016.
  */

/**
  * Trait that every message that belongs to administration operations has to extend.
  *
  * @see QueryMessage
  * @see AdminPermissionMessage
  */
trait AdminMessage extends QueryMessage with AdminPermissionMessage { }
