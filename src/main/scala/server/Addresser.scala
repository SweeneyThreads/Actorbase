package server

import java.util

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

import scala.collection.immutable.{List, HashMap}
import scala.collection.mutable.{ListBuffer, MutableList}


/**
  * Created by Paolo on 31/05/2016.
  */

class Addresser extends Actor with ActorLogging{

  val cluster = Cluster(context.system)
  var nNodes: Integer = 0
  var counter: Integer = 1
  var addresses: ListBuffer[Address] = ListBuffer()

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
      addresses = addresses += cluster.selfAddress
      nNodes = addresses.length
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case "GiveItToMeBitch" =>
      sender() ! nextAddress()
    case MemberUp(member) =>
      addresses = addresses+=member.address
      nNodes=addresses.length
    //  case UnreachableMember(member) =>
    //    log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      for(i <- 0 to addresses.length){
        if (addresses(i) == member.address){
          addresses.remove(i)
          nNodes=addresses.length
        }
      }
    case _ => // ignore
  }

  def nextAddress(): Address = {
    var aux: Address = null
      aux= addresses(counter%nNodes)
      counter=counter+1
    return aux
  }

}