package server.messages.query

import server.enums.EnumReplyResult.ReplyResult

/**
  * Created by matteobortolazzo on 13/05/2016.
  */
case class ReplyMessage(result: ReplyResult, question: QueryMessage, info: String)