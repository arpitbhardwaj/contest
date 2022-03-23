package com.ab
package infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object MailboxesDemo extends App {

  val system = ActorSystem("MailboxesDemo", ConfigFactory.load("infrastructure/mailboxesDemo.conf"))

  class SimpleActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case m => log.info(m.toString)
    }
  }

  //Step 1: Mailbox definition
  class SupportTicketPriorityMailbox(settings:ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    PriorityGenerator{
      case message: String if message.startsWith("[P0]") => 0
      case message: String if message.startsWith("[P1]") => 1
      case message: String if message.startsWith("[P2]") => 2
      case message: String if message.startsWith("[P3]") => 3
      case _ => 4
    }
  )

  //Step 2: Make it known in the config
  //Step 3: Attach the dispatcher to an actor

  val actor = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
  actor ! PoisonPill //this will postponed
  //Thread.sleep(1000)
  actor ! "[P3] this thing would be nice to have"
  actor ! "[P0] this need to be solved now"
  actor ! "[P1] do this when you have the time"

  Thread.sleep(1000)

  /*
  control-aware mailbox
  we will use UnboundedControlAwareMailbox
   */

  //Step 1: Mark important messages as control messages
  case object ManagementTicket extends ControlMessage

  //Step 2: Configure who gets the mailbox
  //- make the actor attach to the mailbox
  val system2 = ActorSystem("MailboxesDemo", ConfigFactory.load("infrastructure/mailboxesDemo.conf")
  .getConfig("mailboxesDemo"))

  //method 1
  val controlAwareActor = system2.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))

  controlAwareActor ! "[P3] this thing would be nice to have"
  controlAwareActor ! "[P0] this need to be solved now"
  controlAwareActor ! "[P1] do this when you have the time"
  controlAwareActor ! ManagementTicket

  Thread.sleep(1000)

  //method 2 : use deployment config
  val altControlAwareActor = system2.actorOf(Props[SimpleActor], "altControlAwareActor")
  altControlAwareActor ! "[P3] this thing would be nice to have"
  altControlAwareActor ! "[P0] this need to be solved now"
  altControlAwareActor ! "[P1] do this when you have the time"
  altControlAwareActor ! ManagementTicket
}
