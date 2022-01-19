package com.ab
package actors

import akka.actor.{Actor, ActorSystem, Props}

object AkkaIntro extends App {
  //part-1 actor system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  //part-2 create actors
  //word count actor

  class WordCountActor extends Actor {
    //internal data
    var totalWords = 0

    //behaviour
    //PartialFunction[Any,Unit] is aliased by Receive
    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[word counter] i have received: $message")
        totalWords += message.split(" ").length
      case message => println(s"I can't understand the ${message.toString}")
    }
  }

  //part-3 instantiate an actor
  //! is tell
  val wordCount1 = actorSystem.actorOf(Props[WordCountActor], "wordCounter1")
  val wordCount2 = actorSystem.actorOf(Props[WordCountActor], "wordCounter2")

  //part-4 communicate (asynchronous)
  wordCount1 ! "I am learning akka and it's pretty damn cool"
  wordCount2 ! "Yayy"

  //instantiate actors with constructor arguments

}
