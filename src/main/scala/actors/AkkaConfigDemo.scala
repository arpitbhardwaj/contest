package com.ab
package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging
import com.typesafe.config.ConfigFactory

/**
 *
 */
object AkkaConfigDemo extends App {

  class SimpleActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }
  //#1. Inline Configuration
  val configStr =
    """
      |akka {
      | loglevel = "ERROR"
      |}
    """.stripMargin
  val config = ConfigFactory.parseString(configStr)
  val system = ActorSystem("AkkaConfigDemo", ConfigFactory.load(config))

  val actor = system.actorOf(Props[SimpleActor],"SimpleActor")
  actor ! "A message to remember"

  //#2. Config File
  val system2 = ActorSystem("AkkaDefaultConfigDemo")
  val actor2 = system2.actorOf(Props[SimpleActor],"SimpleActor2")
  actor2 ! "Remember me"

  //#3. Separate config in same file
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val system3 = ActorSystem("AkkaSpecialConfigDemo",specialConfig)
  val actor3 = system3.actorOf(Props[SimpleActor],"SimpleActor3")
  actor3 ! "Remember me, I am special"

  //#3. Separate config in another file
  val specialConfig2 = ConfigFactory.load("akkaConfigDemo.conf")
  val system4 = ActorSystem("AkkaSpecialConfigDemo",specialConfig2)
  val actor4 = system4.actorOf(Props[SimpleActor],"SimpleActor4")
  println(s"Separate config log level: ${specialConfig2.getString("akka.loglevel")}")
  actor4 ! "Remember me, I am very special"

  //#4 Different file formats (JSON, properties etc)
}
