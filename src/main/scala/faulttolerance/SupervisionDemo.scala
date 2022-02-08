package com.ab
package faulttolerance

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorSystem, AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy}
import akka.testkit.{ImplicitSender, TestKit}

/**
 * When a actor fails, it
 *  suspend its children
 *  send a special message to its children
 *
 * The Parent can decide
 *  resume the actor
 *  restart the actor(default)
 *  stop the actor
 *  escalate and fail itself
 */
object SupervisionDemo extends App {

  class Supervisor extends Actor{

    //OneForOne applies decision to only actor which is failed
    override val supervisorStrategy:SupervisorStrategy = OneForOneStrategy(){
      case _:NullPointerException     => Restart
      case _:IllegalArgumentException => Stop
      case _:RuntimeException         => Resume
      case _:Exception                => Escalate //escalates to user guardian which default restart everything
    }

    override def receive: Receive = {
      case props: Props =>
        val childRef = context.actorOf(props)
        sender() ! childRef
    }
  }

  class NoDeathOnRestartSupervisor extends Supervisor{
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      //overriding with empty as we don't want child to be killed
    }
  }

  class AllForOneSupervisor extends Supervisor {

    //AllForOne applies decision to all actor regardless of which ever failed
    override val supervisorStrategy = AllForOneStrategy(){
      case _:NullPointerException     => Restart
      case _:IllegalArgumentException => Stop
      case _:RuntimeException         => Resume
      case _:Exception                => Escalate //escalates to user guardian which default restart everything
    }
  }

  case object Report
  class FussyWordCounter extends Actor{
    var words = 0
    override def receive: Receive = {
      case Report => sender() ! words
      case "" => throw new NullPointerException("The sentence is empty")
      case sentence:String =>
        if(sentence.length > 20) throw new RuntimeException("sentence is too big")
        else if(!Character.isUpperCase(sentence(0))) throw new IllegalArgumentException("sentence must start with uppercase")
        else words += sentence.split(" ").length
      case _ => throw new Exception("can only process strings")
    }
  }
}
