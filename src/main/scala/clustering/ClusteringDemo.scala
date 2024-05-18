package com.ab
package clustering

import akka.actor.{Actor, ActorLogging, ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberJoined, MemberRemoved, MemberUp, UnreachableMember}
import com.typesafe.config.ConfigFactory

/**
 * @author Arpit Bhardwaj
 *
 */

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
      log.info(s"Let's say welcome to the newest member: ${member.address}")
    case MemberRemoved(member, previousStatus) =>
      log.info(s"Poor ${member.address}, it was removed from $previousStatus")
    case UnreachableMember(member) =>
      log.info(s"Uh oh member ${member.address} is unreachable")
    case m: MemberEvent =>
      log.info(s"Another member event: $m")
  }
}

object ClusteringDemo extends App {
  private def startCluster(ports: List[Int]):Unit = {
    ports.foreach{ port =>
      val config = ConfigFactory.parseString(
        s"""
           |akka.remote.artery.canonical.port = $port
           |""".stripMargin
      ).withFallback(ConfigFactory.load("clustering/clusteringDemo.conf"))

      val system = ActorSystem("AbixelCluster",config)  //all the actor system in a cluster must have same name
      //system.actorOf(Props[ClusterSubscriber], "clusterSubscriber")
    }
  }

  startCluster(List(2551,2552,0)) //for 0 system will allocate the port for you
}

object ManualRegistration extends App{
  val system = ActorSystem(
    "AbixelCluster",
    ConfigFactory
      .load("clustering/clusteringDemo.conf")
      .getConfig("manualRegistration")
  )
  val cluster = Cluster(system)

  joinExistingCluster
  //joinExistingNode
  //joinMyself

  system.actorOf(Props[ClusterSubscriber], "clusterSubscriber")

  def joinExistingCluster =
    cluster.joinSeedNodes(List(
      Address("akka", "AbixelCluster", "localhost", 2551), //"akka://AbixelCluster@localhost:2551"
      Address("akka", "AbixelCluster", "localhost", 2552)
    ))

  def joinExistingNode =
    cluster.join(Address("akka", "AbixelCluster", "localhost", 51038))

  def joinMyself =
    cluster.join(Address("akka", "AbixelCluster", "localhost",2555))
}
