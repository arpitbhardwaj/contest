package com.ab
package streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, Zip}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object GraphBasics extends App {
  implicit val system = ActorSystem("GraphBasics")
  implicit val materializer = ActorMaterializer
  import system.dispatcher

  val input = Source(1 to 1000)
  val incrementer = Flow[Int].map(_ + 1)
  val multiplier = Flow[Int].map(_ * 10)
  val output = Sink.foreach[(Int,Int)](println)

  //Step 1 : Setting up the fundamentals for the graph
  val graph = RunnableGraph.fromGraph(
    GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] => // builder - Mutable DS
      import GraphDSL.Implicits._

      //Step 2 Add the necessary components
      val broadcast = builder.add(Broadcast[Int](2))
      val zip = builder.add(Zip[Int, Int])

      //Step 3 Tying up the components
      input ~> broadcast
      broadcast.out(0) ~> incrementer ~> zip.in0
      broadcast.out(1) ~> multiplier ~> zip.in1

      zip.out ~> output

      ClosedShape //freeze the builder shape means it becomes immutable
      //shape
    } //graph
  ) //runnable graph
  //graph.run()

  /**
   * Exercise: 1
   * feed a source into 2 sinks at the same time
   */

  val firstSink = Sink.foreach[Int](x => println(s"First Sink: $x"))
  val secondSink = Sink.foreach[Int](x => println(s"Second Sink: $x"))

  //Step 1 : Setting up the fundamentals for the graph
  val sourceToTwoSink = RunnableGraph.fromGraph(
    GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] => // builder - Mutable DS
      import GraphDSL.Implicits._

      //Step 2 Add the necessary components
      val broadcast = builder.add(Broadcast[Int](2))

      //Step 3 Tying up the components
      //input ~> broadcast
      //broadcast.out(0) ~> firstSink
      //broadcast.out(1) ~> secondSink

      //syntactic sugar (implicit port numbering)
      input ~>  broadcast ~> firstSink
                broadcast ~> secondSink

      ClosedShape //freeze the builder shape means it becomes immutable
      //shape
    } //graph
  ) //runnable graph
  //sourceToTwoSink.run()


  /**
   * Exercise: 2
   * balance
   */

  val fastSource = input.throttle(5, 1 second)
  val slowSource = input.throttle(2, 1 second)

  val firstSinkCounter = Sink.fold[Int, Int](0)((count, _) => {
    println(s"Sink 1 no of elements: $count")
    count + 1
  })
  val secondSinkCounter = Sink.fold[Int, Int](0)((count, _) => {
    println(s"Sink 2 no of elements: $count")
    count + 1
  })

  //Step 1 : Setting up the fundamentals for the graph
  val balance = RunnableGraph.fromGraph(
    GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] => // builder - Mutable DS
      import GraphDSL.Implicits._

      //Step 2 Add the necessary components
      val merge = builder.add(Merge[Int](2))
      val balance = builder.add(Balance[Int](2))
      //Step 3 Tying up the components
      /*
      fastSource ~> merge ~> balance ~> firstSink
      slowSource ~> merge
      balance ~> secondSink
      */
      fastSource ~> merge ~> balance ~> firstSinkCounter
      slowSource ~> merge
      balance ~> secondSinkCounter

      ClosedShape //freeze the builder shape means it becomes immutable
      //shape
    } //graph
  ) //runnable graph
  balance.run()
}
