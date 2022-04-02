package com.ab
package streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

/**
 * A graph is a blueprint
 * Running a graph allocates the right resources
 *    actor, thread pools
 *    socket, connections
 * Running a graph = materializing which yields a materialized value
 * Materializing a graph = materializing all components
 *    each component produces a materialized value
 *    the graph produces a single materialized value
 *    our job choose which one to pick
 *
 * A component can materialize multiple times
 *    you can reuse the same component multiple times
 *    different run = different materializations
 *
 * A materialized value can be anything
 *
 */
object MaterializeDemo extends App {
  implicit val system = ActorSystem("MaterializeDemo")
  implicit val materializer = ActorMaterializer
  import system.dispatcher

  val source = Source(1 to 10)
  val sink = Sink.foreach[Int](println)
  val graph = source.to(sink)
  //val simpleMaterializedValue = graph.run()

  val sinkReduce = Sink.reduce[Int]((a,b) => a+b)
  /*
  val sumFuture = source.runWith(sinkReduce)
  sumFuture.onComplete{
    case Success(value) =>println(s"the sum of all elements is $value")
    case Failure(exception) => println(s"the sum could not be computed ${exception.getMessage}")
  }
   */

  //choose the materialized value
  val simpleSource = Source(1 to 10)
  val simpleFLow = Flow[Int].map(x=> x+1)
  val simpleSink = Sink.foreach[Int](println)
  //simpleSource.viaMat(simpleFLow)((sourceMat, flowMat)=> flowMat)
  //Keep.none return NotUsed
  //Keep.both returns tuple

  val graph2 = simpleSource.viaMat(simpleFLow)(Keep.right).toMat(simpleSink)(Keep.right)
  graph2.run().onComplete{
    case Success(_) => println("Stream processing finished")
    case Failure(exception) => println(s"Stream processing failed with $exception")
  }

  //if you use via or 2 default it will pick the lest most materialized value
  //if you use viaMat or toMat it will let you choose which materialized value to pick
  //if you use runWith, runReduce it will pick the right most materialized value
  //syntactic sugars
  //Source(1 to 10).runWith(Sink.reduce(_ + _))
  //Source(1 to 10).runReduce(_ + _)

  //backwards
  Sink.foreach[Int](println).runWith(Source.single(42)) //source(..).to(sink(..)).run()
  //both ways
  Flow[Int].map(x => 2*x).runWith(simpleSource, simpleSink)

  /**
   * return the last element out of the source (use Sink.last)
   * compute the total word count out of stream of sentences
   *    -map, fold, reduce
   */
}
