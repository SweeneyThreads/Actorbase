package server.actors

import akka.actor.{Actor, ActorLogging}
import sun.management.counter.Units

/**
  * Created by lucan on 10/05/2016.
  */
class Manager extends Actor with ActorLogging{

  def receive = {
    case _ => ()
  }
}
