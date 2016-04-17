package Server

import java.util.logging.{FileHandler, Logger, SimpleFormatter}

/**
  * Created by lucan on 17/04/2016.
  */

object ActorbaseServer extends App {

  var myLog : Logger = Logger.getLogger("myLog")
  var fileHandler : FileHandler = new FileHandler("./MyLogFile.log")
  myLog.addHandler(fileHandler)
  var formatter : SimpleFormatter = new SimpleFormatter
  fileHandler.setFormatter(formatter)

  myLog.info("Server Started correctly! :) Have fun!")

  myLog.setUseParentHandlers(false);
}
