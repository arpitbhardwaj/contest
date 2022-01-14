package com.ab

/**
 *
 * ActorSystem:
 *    is a heavy weight data structure which controls the number of threads under the hood
 *    threads will be be allocated to running actors
 *    recommend to have one actor system but you can have many
 *    name should be alphanumeric with non leading hyphens and underscores
 *
 * Actors
 *    are uniquely identified
 *    messages are asynchronous
 *    each actors may respond differently
 *    actors are really encapsulated
 *    name should be alphanumeric with non leading hyphens and underscores
 */

import akka.actor.{Actor, ActorSystem, Props}

object AkkaIntro extends App {
  //part-1 actor system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  //part-2 create actors
  //word count actor

  class WordCountActor extends Actor{
    //internal data
    var totalWords = 0
    //behaviour
    //PartialFunction[Any,Unit] is aliased by Receive
    def receive: PartialFunction[Any,Unit] = {
      case message:String =>
        println(s"[word counter] i have received: $message")
        totalWords += message.split(" ").length
      case message => println(s"I can't understand the ${message.toString}")
    }
  }

  //part-3 instantiate an actor
  //! is tell
  val wordCount1 = actorSystem.actorOf(Props[WordCountActor],"wordCounter1")
  val wordCount2 = actorSystem.actorOf(Props[WordCountActor],"wordCounter2")

  //part-4 communicate (asynchronous)
  wordCount1 ! "I am learning akka and it's pretty damn cool"
  wordCount2 ! "Yayy"

  //instantiate actors with constructor arguments

}
