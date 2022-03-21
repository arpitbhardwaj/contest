package com.ab
package infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, Broadcast, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory

/**
 * Supported options for routing logic
 *  round-robin
 *  random
 *  smallest mailbox
 *  broadcast
 *  scatter-gather-first
 *  tail-chopping
 *  consistent-hashing
 *
 */
object RoutersDemo extends App {

  class Master extends Actor{
    //Step 1: Create routees
    //creating 5 actor routees based on slave actor
    private val slaves = for(i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave_$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    //Step 2: Define Router
    private val router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      //Step 3: Route the message
      case msg =>
        router.route(msg, sender())
      //Step 4: handle the termination/lifecycle of the routees
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router.addRoutee(newSlave)
    }
  }

  class Slave extends Actor with ActorLogging{
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("RoutersDemo")
  val master = system.actorOf(Props[Master])

  /*for(i <- 1 to 10){
    master ! s"Hello_$i from the world"
  }*/

  /*
  Method 2.1: a router actor actor with its own children
  Pool Router
   */
  val poolMaster1 = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "poolMaster1")
  /*for(i <- 1 to 10){
    poolMaster1 ! s"Hello_$i from the world"
  }*/

  //Method 2.2: From configuration
  val system2 = ActorSystem("RoutersDemo", ConfigFactory.load("infrastructure/routersDemo.conf")
    .getConfig("routersDemo"))

  val poolMaster2 = system2.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")

  /*for(i <- 1 to 10){
    poolMaster2 ! s"Hello_$i from the world"
  }*/

  /*
  Method 3.1: - routers with actors created elsewhere
  Group Router
   */
  //..in anotther part of my application
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Slave], s"slave_${i}")).toList
  //need their path
  val slavePaths = slaveList.map(slaveRef => slaveRef.path.toString)
  val groupMaster1 = system.actorOf(RoundRobinGroup(slavePaths).props(), "groupMaster1")

  /*for(i <- 1 to 10){
    groupMaster1 ! s"Hello_$i from the world"
  }*/

  //Method 3.2 From config
  val groupMaster2 = system2.actorOf(FromConfig.props(), "groupMaster2")

  /*for(i <- 1 to 10){
    groupMaster2 ! s"Hello_$i from the world"
  }*/

  /*
  Special Messages
   */

  groupMaster2 ! Broadcast("hello, everyone")
  //PoisonPill and Kill are not routed
  //AddRoutee, RemoveRoutee, GetRoutee handled only by the routing actor
}
