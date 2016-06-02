package server.messages.query.user

import server.messages.query.QueryMessage

/**
  * Created by lucan on 10/05/2016.
  */

/**
  * Trait that every message which belongs to user operations has to extend.
  *
  * @see QueryMessage
  */
trait UserMessage extends QueryMessage {}
