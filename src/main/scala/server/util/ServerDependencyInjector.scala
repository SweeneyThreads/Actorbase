package server.util

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorRef
import server.Server

/**
  * Created by kurt on 11/05/2016.
  */
trait ServerDependencyInjector {
  def getStoremanagers : ConcurrentHashMap[String, ActorRef] = null
}

trait StandardServerInjector extends ServerDependencyInjector {
  override def getStoremanagers : ConcurrentHashMap[String, ActorRef] = {
    Server.storemanagers
  }
}

