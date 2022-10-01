package com.ab
package testingakka

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

class TimedAssertionSpec
  extends TestKit(ActorSystem("TimedAssertionSpec", ConfigFactory.load().getConfig("specialTimedAssertionConfig")))//create actor system
  with ImplicitSender //used for send reply scenarios passing implicit testactor for the same
  with AnyWordSpecLike //to write test in very natural language style
  with BeforeAndAfterAll //for hook methods
{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TimedAssertionSpec._

  "A Worker Actor" should {
    val actor = system.actorOf(Props[WorkerActor])
    "reply long work in a timely manner" in {
      within(500 millis, 1 second){
        actor ! "longWork"
        expectMsg(Result(42))
      }
    }

    "reply short work in a reasonably cadence" in {
      within(1 second){
        actor ! "shortWorkSeq"

        val results: Seq[Int] = receiveWhile[Int](max = 2 seconds, idle = 500 millis, messages = 10){
          case Result(res) => res
        }

        assert(results.sum == 10)
      }
    }

    "reply long work to test probe in a timely manner" in {
      within(1 second){
        val testProbe = TestProbe()
        testProbe.send(actor, "longWork")
        testProbe.expectMsg(Result(42))
      }
    }
  }
}

object TimedAssertionSpec{

  case class Result(result: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "longWork" =>
        Thread.sleep(500)
        sender() ! Result(42)
      case "shortWorkSeq" =>
        val r = new Random()
        for (_ <- 1 to 10){
          Thread.sleep(r.nextInt(50))
          sender() ! Result(1)
        }
    }
  }
}