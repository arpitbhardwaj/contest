package com.ab
package faulttolerance

import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class SupervisionDemoSpec extends TestKit(ActorSystem("SupervisionDemoSpec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import SupervisionDemo._

  "A supervisor" should{
    "resume its child in case of a minor fault" in{
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      child ! "I love akka"
      child ! Report
      expectMsg(3)

      child ! "Akka is awesome because i am learning to think in a whole new way"
      child ! Report
      expectMsg(3)
    }

    "restart its child in case of a empty sentence" in{
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      child ! "I love akka"
      child ! Report
      expectMsg(3)

      child ! ""
      child ! Report
      expectMsg(0)
    }

    "terminate its child in case of a major error" in{
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      watch(child)
      child ! "i love akka"
      val terminated = expectMsgType[Terminated]
      assert(terminated.actor == child)
    }

    "escalate an error when it doesn't know what to do" in{
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      watch(child)
      child ! 42
      val terminated = expectMsgType[Terminated]
      assert(terminated.actor == child)
    }
  }

  "A Kinder supervisor" should {
    "not kill its child in case its restarted or escalates" in {
      val supervisor = system.actorOf(Props[NoDeathOnRestartSupervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      child ! "I love akka"
      child ! Report
      expectMsg(3)

      child ! 45
      child ! Report
      expectMsg(0)
    }
  }

  "A all-for-one supervisor" should {
    "apply the all in one strategy" in {
      val supervisor = system.actorOf(Props[AllForOneSupervisor],"allForOneSupervisor")
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      supervisor ! Props[FussyWordCounter]
      val child2 = expectMsgType[ActorRef]

      child2 ! "I love akka"
      child2 ! Report
      expectMsg(3)

      EventFilter[NullPointerException]() intercept {
        child ! ""
      }

      Thread.sleep(100)
      child2 ! Report
      expectMsg(0)
    }
  }

}
