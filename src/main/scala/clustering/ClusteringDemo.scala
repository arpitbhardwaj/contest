package com.ab
package clustering

import akka.actor.{Actor, ActorLogging, ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberJoined, MemberRemoved, MemberUp, UnreachableMember}
import com.typesafe.config.ConfigFactory

/**
 * Build distributed applications
 *  decentralized peer to peer
 *  no single point of failure
 *  automatic node membership and gossip protocol
 *  failure detector
 *
 *  Clustering is based on remoting
 *    in most cases use clustering instead of remoting
 *
 * Clusters
 *  composed of member nodes
 *    node = host + port + UID
 *    on the same jvm
 *    on multiple jvms on the same machine
 *    on a set of machine of any scale
 *
 * Cluster membership
 *  convergent gossip protocol
 *  phi accrual failure detector - same as remoting
 *  no leader election - leader is deterministically chosen
 *
 * Join a cluster
 *  contact seed nodes in order (from config)
 *    if i am the first seed node, i will join myself
 *    send a join command to the seed node that responds first
 *  node is in the joining state
 *    wait for gossip to converge
 *    all nodes in teh cluster must acknowledge the new node
 *  the leader will set the state of new node to up
 *
 * Leave a cluster
 *  Option 1: Safe and quite
 *    node switches its state to leaving
 *    gossip converges
 *    leaders set the state to "existing"
 *    gossip converges
 *    leaders marks it removed
 *  Option 3: The hard way
 *    a node becomes unreachable
 *    gossip convergence and leader actions are not possible
 *    must be removed (download) manually
 *    cal also be auto downed bt the leader
 *    DO NOT USE auto downing in prod
 */
object ClusteringDemo extends App {
  def startCluster(ports: List[Int]):Unit = {
    ports.foreach{ port =>
      val config = ConfigFactory.parseString(
        s"""
          |akka.remote.artery.canonical.port = $port
          |""".stripMargin
      ).withFallback(ConfigFactory.load("clustering/clusteringBasics.conf"))

      val system = ActorSystem("AbixelCluster",config)
      system.actorOf(Props[ClusterSubscriber], "clusterSubscriber")
    }
  }

  startCluster(List(2551,2552,0))
}

class ClusterSubscriber extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(
      self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember]
    )
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive: Receive = {
    case MemberJoined(member) =>
      log.info(s"New member in town ${member.address}")
    case MemberUp(member) if member.hasRole("numberCruncher") =>
      log.info(s"Hello Brother: ${member.address}")
    case MemberUp(member) =>
      log.info(s"Lets say welcome to the newest member: ${member.address}")
    case MemberRemoved(member, previousStatus) =>
      log.info(s"Poor ${member.address}, it was removed from $previousStatus")
    case UnreachableMember(member) =>
      log.info(s"Uh oh member ${member.address} is unreachable")
    case m: MemberEvent =>
      log.info(s"Another mmeber event: $m")
  }
}

object ManualRegistration extends App{
  val system = ActorSystem(
    "AbixelCluster",
    ConfigFactory
      .load("clustering/clusteringBasics.conf")
      .getConfig("manualRegistration")
  )
  val cluster = Cluster(system)
  def joinExistingCluster =
    cluster.joinSeedNodes(List(
      Address("akka", "AbixelCluster", "localhost", 2551), //"akka://AbixelCluster@localhost:2551"
      Address("akka", "AbixelCluster", "localhost", 2552)
    ))


  def joinExistingNode =
    cluster.join(Address("akka", "AbixelCluster", "localhost", 51038))

  def joinMyself =
    cluster.join(Address("akka", "AbixelCluster", "localhost",2555))

  joinExistingCluster
  //joinExistingNode
  //joinMyself
  system.actorOf(Props[ClusterSubscriber], "clusterSubscriber")
}
