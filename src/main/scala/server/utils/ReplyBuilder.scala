package server.utils

import server.messages.query.ReplyMessage

/**
  * Created by borto on 26/05/2016.
  */
class ReplyBuilder {
  def buildReply(reply: ReplyMessage) : String = {
    ""
  }

  def unhandledMessage(actor: String, method: String) : String = {
    "Unhandled message in actor; " + actor + ", method: " + method
  }
}
