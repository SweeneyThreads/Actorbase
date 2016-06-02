package server.messages.query

import server.enums.EnumReplyResult.ReplyResult

/**
  * Created by matteobortolazzo on 13/05/2016.
  */

/**
  * Trait that every message that is used as response info  has to extend.
  */
trait ReplyInfo

/**
  * A ReplyMessage is used as response to an operation request.
  * @param result The result of the operation
  * @param question The operation requested
  * @param info The information about the response
  */
case class ReplyMessage(result: ReplyResult, question: QueryMessage, info: ReplyInfo = null)