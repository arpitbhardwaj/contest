package com.ab
package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActors extends App {

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(msg: String)
  }

  class Parent extends Actor {

    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child...")
        val child = context.actorOf(Props[Child], name)
        context.become(withChild(child))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(msg) => childRef forward (msg)
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case msg => println(s"${self.path} I got : $msg")
    }
  }

  val system = ActorSystem("childActorsSystem")
  val parent = system.actorOf(Props[Parent], "parent")

  import Parent._

  parent ! CreateChild("child")
  parent ! TellChild("Hey Kid!")

  /*
  Guardian Actors (top-level)
  - /system = system guardian
  - /user = user level guardian (manages the actors which we create)
  - / = root guardian (if this throws exception the whole actor system dies)
   */

  //Actor Selection (is a wrapper over potential actor ref)
  val childSelection = system.actorSelection("/user/parent/child")
  childSelection ! "I found you"

  /*
  DANGER

  Never pass mutable actor state, or this reference to child actors
   */


}
