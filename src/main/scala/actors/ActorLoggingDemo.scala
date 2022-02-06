package com.ab
package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

/**
 * Logging is asynchronous
 * you can change the logger e.g slf4j
 *
 */
object ActorLoggingDemo extends App {

  //#1 Explicit Logging
  class ActorWithExplicitLogging extends Actor{
    val looger = Logging(context.system,this)
    override def receive: Receive = {
          /*
          1- Debug
          2- Info
          3- Warn
          4- Error
           */
      case message => looger.info(message.toString)
    }
  }

  //#2 ActorLogging
  class ActorWithActorLogging extends Actor with ActorLogging{
    override def receive: Receive = {
      case (a,b) => log.info("Two things: {} and {}",a,b)
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("ActorLoggingDemo")
  val actor = system.actorOf(Props[ActorWithExplicitLogging], "actorWithExplicitLogging")
  actor ! "Logging a simple message"

  val actor2 = system.actorOf(Props[ActorWithActorLogging], "actorWithActorLogging")
  actor2 ! "Logging another simple message"
  actor2 ! (42,62)
}
