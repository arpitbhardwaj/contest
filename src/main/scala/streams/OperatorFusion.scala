package com.ab
package streams

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object OperatorFusion extends App {
  implicit val system = ActorSystem("OperatorFusion")
  implicit val materializer = ActorMaterializer
  import system.dispatcher


  val simpleSource = Source(1 to 1000)
  val simpleFLow1 = Flow[Int].map(_ + 1)
  val simpleFLow2 = Flow[Int].map(_ + 10)
  val simpleSink = Sink.foreach[Int](println)

  //this runs on the same actor
  //simpleSource.via(simpleFLow1).via(simpleFLow2).to(simpleSink).run()
  //operator/component fusion done by default

  //*equivalent behaviour as above*
  class SimpleActor extends Actor{
    override def receive: Receive = {
      case x:Int =>
        val x2 = x+1
        val x3 = x2+10
        println(x3)
    }
  }

  //val simpleActor = system.actorOf(Props[SimpleActor])
  //(1 to 1000).foreach(simpleActor ! _)

  //complex flows
  val complexFLow1 = Flow[Int].map { x =>
    Thread.sleep(1000)
    x + 1
  }

  val complexFLow2 = Flow[Int].map { x =>
    Thread.sleep(1000)
    x + 1
  }

  //throughput is bad if the operation are complex
  //simpleSource.via(complexFLow1).via(complexFLow2).to(simpleSink).run()

  //use async boundaries
  /*
  simpleSource.via(complexFLow1).async //runs on first actor
    .via(complexFLow2).async //runs on second actor
    .to(simpleSink).run() // runs on third actor
  */

  //ordering guarantees
  Source(1 to 3)
    .map(ele => {println(s"Flow A: $ele"); ele})
    .map(ele => {println(s"Flow B: $ele"); ele})
    .map(ele => {println(s"Flow C: $ele"); ele})
    .runWith(Sink.ignore)

  //ordering not guaranteed
  Source(1 to 3)
    .map(ele => {println(s"Flow A: $ele"); ele}).async
    .map(ele => {println(s"Flow B: $ele"); ele}).async
    .map(ele => {println(s"Flow C: $ele"); ele}).async
    .runWith(Sink.ignore)
}
