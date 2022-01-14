package com.ab

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" => context.sender() ! "Hello, there" //replying to a message
      case msg:String => println(s"I have received a string msg $msg")
      case msg:Int => println(s"I have received a number msg $msg")
      case SpecialMessage(contents) => println(s"I have received special msg $contents")
      case msg:Double => println(s"${context.self} I have received a double msg $msg")
      case msg:Char => println(s"${self} I have received a char msg $msg")
      case msg:Boolean => println(s"${context.self.path} I have received a boolean msg $msg")
      case SendMessageToYourself(contents) => self ! contents
      case SendMessageTo(ref) => ref ! "Hi"
      case ForwarMessage(contents,ref) => ref forward (contents + "s") //keep the original sender of the ForwardMessage
    }
  }

  val actorSystem = ActorSystem("actorSystemDemo")
  val simpleActor = actorSystem.actorOf(Props[SimpleActor],"simpleActor")
  simpleActor ! "Hello"

  //Capability-1 message can be of any type following below condition
    //a) message must be immutable
    //b) message must be serializable
    //best practice use case classes and case objects
  simpleActor ! 42

  case  class SpecialMessage(contents:String)
  simpleActor ! SpecialMessage("special content")

  //Capability-2 actors have information about their context and about themselves
  //context.self == this in OOP
  simpleActor ! 43.7
  simpleActor ! 'c'
  simpleActor ! true

  //Capability-3 self can be used to send to himself
  case class SendMessageToYourself(contents:String)
  simpleActor ! SendMessageToYourself("I am an actor and i am proud of it")

  //Capability-4 actors can reply to messages
  val alice = actorSystem.actorOf(Props[SimpleActor],"alice")
  val bob = actorSystem.actorOf(Props[SimpleActor],"bob")
  case class SendMessageTo(ref:ActorRef)
  alice ! SendMessageTo(bob)

  //Capability-5 deadletters is a fake actor in akka which takes care to receive the message which not sent to anyone
  //garbage pool of messages in akka
  alice ! "Hi" //who is the sender (noSender)

  //Capability-6 forwarding messages
  //Daniel->Alice->Bob
  //forwarding- sending a message with the original sender
  case class ForwarMessage(contents:String, ref:ActorRef)
  alice ! ForwarMessage("Hi", bob)
}
