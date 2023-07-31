package com.ab
package actors

import akka.actor.{Actor, ActorSystem, DeadLetter, PoisonPill, Props, UnhandledMessage}

case object Message
case object InValidMessage

object DeadLetter {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("ActorSystem")
    val driverActor = system.actorOf(Props[MainActor], "mainActor")
    val listener = system.actorOf(Props[DeadActorListener], "deadActor")
    system.eventStream.subscribe(listener, classOf[UnhandledMessage])
    system.eventStream.subscribe(listener, classOf[DeadLetter])

    driverActor ! Message
    driverActor ! InValidMessage
    driverActor ! Message
    driverActor ! PoisonPill
    driverActor ! Message
  }
}

class MainActor extends Actor {
  def receive = {
    case Message => {
      println("Message Received")
    }
  }
}

class DeadActorListener extends Actor {
  def receive = {
    case u: UnhandledMessage => println("Unhandled message " + u.message)
    case d: DeadLetter => println("dead message " + d.message)
  }
}
