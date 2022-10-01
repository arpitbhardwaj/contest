package com.ab
package testingakka

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

/**
 *
 * testActor is the member of TestKit and is responsible for the communication (sending and receiving the messages)
 */
class BasicSpec
extends TestKit(ActorSystem("BasicSpec"))//create actor system
with ImplicitSender //used for send reply scenarios passing implicit testactor for the same
with AnyWordSpecLike //to write test in very natural language style
with BeforeAndAfterAll //for hook methods
{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

  //test suite
  "A simple actor" should {
    //testing scenario
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val msg = "Hello Test"
      echoActor ! msg
      expectMsg(msg)
    }
  }

  "A blackhole actor" should {
    //testing scenario
    "send back the same message" in {
      val blackholeActor = system.actorOf(Props[BlackHoleActor])
      val msg = "Hello Test"
      blackholeActor ! msg
      //expectMsg(msg)
      expectNoMessage(1 second)
    }
  }

  //message assertions
  "A lab test actor" should {

    val labTestActor = system.actorOf(Props[LabTestActor])

    //testing scenario
    "turn a string to uppercase" in {

      val msg = "I Love Akka"
      labTestActor ! msg
      expectMsg("I LOVE AKKA")
      //or
      val reply = expectMsgType[String]
      assert(reply == "I LOVE AKKA")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply to favourite tech" in {
      labTestActor ! "favouriteTech"
      expectMsgAllOf("Scala","Akka")
    }

    "reply to favourite tech in different way" in {
      labTestActor ! "favouriteTech"
      val msg = receiveN(2)
      //free to do more complicated assertions
    }

    "reply to favourite tech in fancy way" in {
      labTestActor ! "favouriteTech"
      expectMsgPF() {
        case "Scala" =>
        case "Akka" =>
      }
    }

  }
}

// create companion object for specs (to store all info methods and values which you are going to use in test)
object BasicSpec{

  class SimpleActor extends Actor{
    override def receive: Receive = {
      case msg => sender() ! msg
    }
  }

  class BlackHoleActor extends Actor{
    override def receive: Receive = {
      Actor.emptyBehavior
    }
  }

  class LabTestActor extends Actor{
    val random = new Random()
    override def receive: Receive = {
      case "greeting" =>
        if(random.nextBoolean())
          sender() ! "hi"
        else
          sender() ! "hello"
      case "favouriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case msg:String => sender() ! msg.toUpperCase
    }
  }
}
