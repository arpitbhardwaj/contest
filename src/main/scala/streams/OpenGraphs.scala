package com.ab
package streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, SourceShape}
import akka.stream.scaladsl.{Concat, GraphDSL, Sink, Source}

object OpenGraphs extends App {

  implicit val system = ActorSystem("GraphBasics")
  implicit val materializer = ActorMaterializer
  import system.dispatcher

  val firstSource = Source(1 to 10)
  val secondSource = Source(42 to 1000)

  val sourceGraph = Source.fromGraph(
    GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      //Step 2 Add the necessary components
      val concat = builder.add(Concat[Int](2))
      //Step 3 Tying up the components
      firstSource ~> concat
      secondSource ~> concat

      SourceShape(concat.out) //open shape
    } //graph
  )
  sourceGraph.to(Sink.foreach(println)).run()
}
