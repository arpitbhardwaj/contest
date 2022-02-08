package com.ab
package faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

/**
 * Actors can be
 * started
 * suspended
 * resumed
 * restarted
 * stopped
 * Start : create a new actor ref with uuid at a given path
 * Suspend : the actor ref will enqueue but not process more messages
 * Resumed : the actor ref will continue processing more messages
 * Restarted :
 * suspend
 * swap the actor instance
 * old instance calls preRestart
 * replace actor instance
 * new instance calls postRestart
 * resume
 * Stopped:
 * calls postStop
 * all watching actors receive Terminated
 *
 * After stopping, another actor may be created at the same path
 * diff UUID, so diff actor ref
 *
 * Default Supervision Strategy:
 *
 */
object ActorLifecycleDemo extends App {

  object StartChild

  class LifecycleActor extends Actor with ActorLogging {
    override def preStart(): Unit = log.info("I am starting")

    override def postStop(): Unit = log.info("i have stopped")

    override def receive: Receive = {
      case StartChild =>
        context.actorOf(Props[LifecycleActor], "child")
    }
  }

  val system = ActorSystem("ActorLifecycleDemo")
  /*val parent = system.actorOf(Props[LifecycleActor], "parent")
  parent ! StartChild
  parent ! PoisonPill*/

  object Fail

  object FailChild

  object Check

  object CheckChild

  class Parent extends Actor {
    val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild => child ! Fail
      case CheckChild => child ! Check
    }
  }

  class Child extends Actor with ActorLogging {
    override def preStart(): Unit = log.info("supervisedChild is starting")

    override def postStop(): Unit = log.info("supervisedChild has been stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"supervised actor restarting because of ${reason.getMessage}")

    override def postRestart(reason: Throwable): Unit =
      log.info("supervised actor restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning("child will fail now")
        throw new RuntimeException("Uh oh i am failed!")
      case Check =>
        log.info("alive and kicking")
    }
  }

  val supervisor = system.actorOf(Props[Parent], "parent")
  supervisor ! FailChild
  supervisor ! CheckChild
}
