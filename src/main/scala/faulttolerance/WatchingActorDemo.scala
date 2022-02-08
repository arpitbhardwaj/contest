package com.ab
package faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props, Terminated}

object WatchingActorDemo extends App {

  object Watcher {
    case class StartChild(name: String)

    case class StopChild(name: String)

    case object Stop
  }

  class Watcher extends Actor with ActorLogging {

    import Watcher._

    override def receive: Receive = {
      case StartChild(name) =>
        log.info(s"Starting and watching child $name")
        val child = context.actorOf(Props[Child], name)
        context.watch(child)
      case Terminated(ref) =>
        log.info(s"The reference that i'm watching $ref has been stopped")
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("WatchingActorDemo")
  val watcher = system.actorOf(Props[Watcher], "watcher")

  import Watcher._

  watcher ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watcher/watchedChild")
  Thread.sleep(100)
  watchedChild ! PoisonPill
}
