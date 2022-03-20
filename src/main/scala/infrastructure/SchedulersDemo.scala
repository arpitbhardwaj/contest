package com.ab
package infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/**
 *
 * In Schedulers
 *  don't use unstable references inside scheduled actions
 *  all scheduled tasks executes when the system is terminated
 *  schedulers are not the best at precision and long term planning
 *
 */
object SchedulersDemo extends App {

  class SimpleActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("SchedulersDemo")
  val actor = system.actorOf(Props[SimpleActor])

  system.log.info("Scheduling reminders for simple actor")

  //implicit val ec = system.dispatcher
  import system.dispatcher
  system.scheduler.scheduleOnce(1 seconds){
    actor ! "reminder"
  }//(system.dispatcher)

  val routine: Cancellable = system.scheduler.schedule(1 second, 2 seconds){
    actor ! "heartbeat"
  }

  system.scheduler.scheduleOnce(5 seconds){
    routine.cancel()
  }

  /*
  Implement a self closing actor
    if the actor receives a message, you have 1 sec to send it another message
    if the time window expires, the actor will stop itself
    if you another message, the time window reset
   */

  class SelfClosingActor extends Actor with ActorLogging{

    var schedule = createTimeoutWindow()

    def createTimeoutWindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1 second){
        self ! "timeout"
      }
    }

    override def receive: Receive = {
      case "timeout" =>
        log.info("Stopping myself")
        context.stop(self)
      case msg =>
        log.info(s"Received $msg, staying alive")
        schedule.cancel()
        schedule = createTimeoutWindow()
    }
  }

  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingActor")
  system.scheduler.scheduleOnce(250 millis){
    selfClosingActor ! "ping"
  }

  system.scheduler.scheduleOnce(2 seconds){
    system.log.info("sending pong to self closing actor")
    selfClosingActor ! "pong"
  }
}
