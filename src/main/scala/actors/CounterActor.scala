package com.ab
package actors

import akka.actor.{Actor, ActorSystem, Props}

/**
 * Counter Actor
 *    Increment
 *    Decrement
 *    Print
 */
object CounterActor extends App {

  object Counter{
    case object Increment
    case object Decrement
    case object Print
  }

  //stateful
  class Counter extends Actor{
    var count = 0
    import Counter._
    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"My counter is $count")
    }
  }

  import Counter._
  val actorSystem = ActorSystem("firstActorSystem")
  val counterActor = actorSystem.actorOf(Props[Counter], "counterActor")

  (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 3).foreach(_ => counterActor ! Decrement)
  counterActor ! Print

  //stateless
  //you need to rewrite its mutable state into the parameters of the receive handlers you want to support
  class NewCounter extends Actor{
    import Counter._

    override def receive: Receive = countReceive(0)

    def countReceive(counter:Int): Receive = {
      case Increment =>
        println(s"[countReceive($counter)] incrementing")
        context.become(countReceive(counter+1))
      case Decrement =>
        println(s"[countReceive($counter)] decrementing")
        context.become(countReceive(counter-1))
      case Print => println(s"[countReceive($counter)] My counter is $counter")
    }
  }

  val newCounterActor = actorSystem.actorOf(Props[NewCounter], "newCounterActor")

  (1 to 5).foreach(_ => newCounterActor ! Increment)
  (1 to 3).foreach(_ => newCounterActor ! Decrement)
  newCounterActor ! Print
}
