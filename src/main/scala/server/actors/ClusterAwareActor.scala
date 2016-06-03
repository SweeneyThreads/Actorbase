package server.actors

import akka.actor.{ActorRef, Actor, Address}
import server.Server
import scala.concurrent.duration._
import akka.dispatch.ExecutionContexts._
import akka.pattern.ask
import akka.util.Timeout

/**
  * Created by Paolo on 02/06/2016.
  */

/**
  * Trait that gives to an actor the nextAddress method,
  * should be extended from actors that needs to create actors in other nodes of the cluster.
  * The policy of addresses selection is responsibility of the ClusterListener of this node.
  * It also includes the values required to use futures.
  *
  * @see ClusterListener
  */
trait ClusterAwareActor extends Actor {
  // Values for futures
  implicit val timeout = Timeout(25 seconds)
  implicit val ec = global

  var clusterListener: ActorRef = Server.clusterListener

  /**
    * Returns an address of one node in the cluster.
    * This method sends a message to the ClusterListener of the same node of this actor
    * so it will stop the thread of this actor while waiting for a response.
    *
    * @return the address of type akka.actor.Address.
    */
  def nextAddress: Address ={
    // auxiliary variable
    var aux :Address = null
    // send a message to the ClusterListener of this node to get an address
    clusterListener ? "next" onSuccess {
      // save the response as an address
      case result => aux = result.asInstanceOf[Address]
    }
    // wait for the response from the ClusterListener
    while(aux == null){
      Thread.sleep(10)
    }
    // return the address
    aux
  }
}
