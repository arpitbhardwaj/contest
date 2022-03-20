package com.ab
package infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Timers}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object TimersDemo extends App {

  object TimerBasedHeartbeatActor{
    case object TimerKey
    case object Start
    case object Reminder
    case object Stop
  }
  class TimerBasedHeartbeatActor extends Actor with ActorLogging with Timers{
    import TimerBasedHeartbeatActor._

    timers.startSingleTimer(TimerKey,Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startSingleTimer(TimerKey,Reminder, 1 second)
      case Reminder =>
        log.info("I'm alive")
      case Stop =>
        log.info("Stopping")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  val system = ActorSystem("TimersDemo")
  val actor = system.actorOf(Props[TimerBasedHeartbeatActor], "timerBasedHeartbeatActor")
  import system.dispatcher
  import TimerBasedHeartbeatActor._
  system.scheduler.scheduleOnce(5 seconds){
    actor ! Stop
  }
}
