package com.ab
package streams

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/**
 *
 *
 */
object Backpressure extends App {

  implicit val system = ActorSystem("Backpressure")
  implicit val materializer = ActorMaterializer
  import system.dispatcher

  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int]{x =>
    Thread.sleep(1000)
    println(s"Sink: $x")
  }
  //fusing opeartors hence not a backpressure
  //val graph = fastSource.to(slowSink).run()

  val simpleFlow = Flow[Int].map{ x =>
    println(s"Incoming: $x")
    x+1
  }

  //the default buffer in akka streams components is 16 elements
  //val graph = fastSource.async.via(simpleFlow).async.to(slowSink).run()

  /**
   * reactions to backpressure in order
   *    try to slow down if possible
   *    buffer elements untill there's more demand
   *    drop down elements from the buffer if it overflows
   *    tear down or kill the home stream in case of failure
   *
   */

  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
  //val graph = fastSource.async.via(bufferedFlow).async.to(slowSink).run()

  /**
   * Overflow Strategies
   *    drop head = oldest
   *    drop tail = newest
   *    drop new = exact element to be added = keeps the buffer
   *    drop the entire buffer
   *    backpressure signal
   *    fail
   */

  //throttling - manually trigger backpressure

  fastSource.throttle(2, 1 second).runWith(Sink.foreach(println))
}
