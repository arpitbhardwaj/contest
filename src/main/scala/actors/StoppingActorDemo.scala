package com.ab
package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props}

object StoppingActorDemo extends App {

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging{
    import Parent._

    override def receive: Receive = withChild(Map())

    def withChild(children: Map[String,ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"Starting child $name")
        context.become(withChild(children + (name -> context.actorOf(Props[Child], name))))
      case StopChild(name) =>
        log.info(s"Stopping child $name")
        val childOption = children.get(name)
        childOption.foreach(childRef => context.stop(childRef)) //asynchronous operation, child will not be stopped immediately
      case Stop =>
        log.info("Stopping myself")
        context.stop(self) //asynchronous operation, all child will be stopped first then the parent
      case msg =>
        log.info(msg.toString)
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("StoppingActorDemo")
  val parent = system.actorOf(Props[Parent], "parent")

  import Parent._

  parent ! StartChild("child1")
  val child = system.actorSelection("/user/parent/child1")
  Thread.sleep(100)
  child ! "Hey Kid"
  parent ! StopChild("child1")
  //for (_ <- 1 to 10) child ! "are you still there?"

  parent ! StartChild("child2")
  val child2 = system.actorSelection("/user/parent/child2")
  Thread.sleep(100)
  child2 ! "Hey 2nd Kid"

  parent ! Stop
  //for (_ <- 1 to 10) parent ! "are you still there?"
  //for (i <- 1 to 100) child2 ! s"[$i] 2nd Kid, are you still there"

  /**
   * Using special messages
   */
  val orphanActor = system.actorOf(Props[Child])
  orphanActor ! "Hello orphan actor"
  orphanActor ! PoisonPill
  orphanActor ! "orphan actor, are you still there?"

  val orphanActor2 = system.actorOf(Props[Child])
  orphanActor2 ! "Hello 2nd orphan actor"
  orphanActor2 ! Kill //kill is more brutal than poison pill, the actor throws ActorKilledException
  orphanActor2 ! "2nd orphan actor, are you still there?"
}
