package com.ab
package testingakka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

/**
 *
 * TestProbe is a special actor with some assertion capabilities
 */
class TestProbeSpec
  extends TestKit(ActorSystem("TestProbeSpec"))//create actor system
  with ImplicitSender //used for send reply scenarios passing implicit testactor for the same
  with AnyWordSpecLike //to write test in very natural language style
  with BeforeAndAfterAll //for hook methods
{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._

  "A master actor" should {
    //Master is a statfull actor hence need to be create under each test case
    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegisterAck)
    }

    "send work to slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegisterAck)

      val workString = "I Love Akka"
      master ! Work(workString)
      slave.expectMsg(SlaveWork(workString, testActor))
      slave.reply(WorkCompleted(3,testActor))

      expectMsg(Report(3))
    }

    "aggregate data correctly" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegisterAck)

      val workString = "I Love Akka"
      master ! Work(workString)
      master ! Work(workString)

      slave.receiveWhile() {
        case SlaveWork(`workString`, `testActor`) => slave.reply(WorkCompleted(3,testActor))
      }

      expectMsg(Report(3))
      expectMsg(Report(6))
    }
  }

}

object TestProbeSpec{
  /*
  word counting actor hierarchy master - slave

  send some work to the master
    master send slave the piece of work
    slave process the work and replies to master
    master aggregates the result
  master send the total count to the requester

   */

  case class Register(slaveRef: ActorRef)
  case object RegisterAck
  case class Work(text:String)
  case class SlaveWork(text:String, originRequester:ActorRef)
  case class WorkCompleted(count:Int, originRequester:ActorRef)
  case class Report(totalCount:Int)

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegisterAck
        context.become(online(slaveRef,0))
      case _ => //ignore
    }

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text,sender())
      case WorkCompleted(count,originRequester) =>
        val newTotalWordCount = count + totalWordCount
        originRequester ! Report(newTotalWordCount)
        context.become(online(slaveRef,newTotalWordCount))
    }
  }

}