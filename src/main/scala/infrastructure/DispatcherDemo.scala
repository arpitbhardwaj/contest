package com.ab
package infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object DispatcherDemo extends App {

  class Counter extends Actor with ActorLogging{
    var count = 0
    override def receive: Receive = {
      case msg =>
        count+=1
        log.info(s"[$count] message")
    }
  }
  val system = ActorSystem("DispatcherDemo", ConfigFactory.load("infrastructure/dispatcherDemo.conf"))

  //Method 1: Programmatic
  //val actors = for(i <- 1 to 10) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")

  //Method 2: From config
  val actors = for(i <- 1 to 10) yield system.actorOf(Props[Counter], s"counter_$i")

  val r = new Random()
  /*for (i <- 1 to 1000){
    actors(r.nextInt(10)) ! i
  }*/

  /**
   * Dispatchers implements the execution context trait
   */

  class BlockingActor extends Actor with ActorLogging{
    implicit val ec: ExecutionContext = context.dispatcher

    //recommend to use your own dispatcher in case of blocking tasks as it will overload the default actors dispatcher
    // which is used for handling messages

    //Solution 1
    //implicit val ec: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher1")

    //Solution 2
    //use routers

    override def receive: Receive = {
      case msg =>
        Future{
          Thread.sleep(5000)
          log.info(s"Success: $msg")
        }
    }
  }

  val blockingActor = system.actorOf(Props[BlockingActor])
  //blockingActor ! "the meaning of life"
  val nonBlockingActor = system.actorOf(Props[Counter])
  for (i <- 1 to 1000){
    val msg = s"Important message $i"
    blockingActor ! msg
    nonBlockingActor ! msg
  }

}
