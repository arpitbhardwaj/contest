package com.ab
package streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

/**
 * Reactive Streams qualities
 *    asynchronous
 *    back pressured
 *    incremental
 *    potentially infinite data processing systems
 *
 * Concepts of streams
 *    publisher = emits elements (asynchronously)
 *    subscriber = receives elements
 *    processor = transform elements along the way
 *    completely async
 *    backpressure
 *
 * Aka Streams
 *    Source = "publisher"
 *      may or may not terminate
 *    Sink = "Subscriber"
 *      only terminates when the source terminates
 *    Flow = "processor"
 *
 * Directions
 *    Upstream => to the source
 *    Downstream => to the Sink
 *
 * Sources can emit any kind of objects as long as they are immutable and serializable
 * nulls are not allowed
 */
object StreamsBasics extends App {
  implicit val system = ActorSystem("StreamsBasics")
  implicit val materializer = ActorMaterializer

  //source
  val source = Source(1 to 10)
  //sink
  val sink = Sink.foreach[Int](println)

  val graph = source.to(sink)
  //graph.run()

  //flows
  val flow = Flow[Int].map(x => x+1)
  val sourceWithFlow = source.via(flow)
  val flowWithSink = flow.to(sink)

  //sourceWithFlow.to(sink).run()
  //source.to(flowWithSink).run()
  source.via(flow).to(sink).run()

  //types of sources
  //val illegalSource = Source.single[String](null)//throws NPE
  val finiteSource1 = Source.single(1)
  val finiteSource2 = Source.single(List(1,2,3))
  val infiniteSource = Source(Stream.from(1))
  val emptySource = Source.empty[Int]

  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.fromFuture(Future(42))

  //sinks
  val boringSink = Sink.ignore
  val forEachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int]//retrives head and closes the stream
  val foldSink = Sink.fold[Int, Int](0)((a,b)=> a+b)

  //flows - usually mapped to collection operators
  val mapFlow = Flow[Int].map(x=> 2*x)
  val takeFLow = Flow[Int].take(5)
  //drop, filter
  //do not have flatmap

  //source -> flow -> flow -> ... -> sink
  val doubleFlowGraph = source.via(mapFlow).via(takeFLow).to(sink)
  doubleFlowGraph.run()

  //syntactic sugars
  val mapSource = Source(1 to 10).map(x => x*2)//equivalent to Source(1to 10).via(Flow[int].map(x => x*2))
  mapSource.runForeach(println) //equivalent to mapSource.to(Sink.foreach[Int](println)).run()

  //Operators =  components

  /**
   * Exercise
   * create a stream that takes the name of the person
   *  the you keep the first 2 names with length >5 characters
   */

}
