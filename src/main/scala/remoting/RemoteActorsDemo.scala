package com.ab
package remoting

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorSystem, Identify, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 *
 * Actor Model Principles
 *  every interaction based on sending messages
 *  full actor encapsulation
 *  no locking
 *  message sending latency
 *  at most once message delivery
 *  message ordering maintained per sender/receive pair
 *
 * The Principles holds
 *  on the same JVM in parallel application
 *  locally on multiple JVMs
 *  in a distributed env on any scale
 *
 *
 * Location Transparency -  the real actor can be anywhere
 * Akka remoting is based on location transparency
 * We communicate with actors via the reference
 *
 * Transparent Remoting - we are using the object as it were local but bts it communicates remotely
 * JAVA RMI uses transparent remoting
 *
 * #artery is akka remoting implementation
 */
object RemoteActorsDemo extends App {

  //two different actor system running on same jvm exposing different ports
  val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("remoting/remoteActors.conf"))

  val localSimpleActor = localSystem.actorOf(Props[SimpleActor],"localSimpleActor")

  localSimpleActor ! "hello, local actor"

  //[akka://LocalSystem@localhost:2551/user/localSimpleActor]
  //[akka://RemoteSystem@localhost:2552/user/remoteSimpleActor]

  //[akka://RemoteSystem@localhost:2552/user/remoteSimpleActor]
  //[{remote artery protocol}{actor system}{remote address}{actor path}]
  //[akka://LocalSystem/user/localSimpleActor]

  //sending message to remote actor

  //Method 1: Actor Selection
  val remoteActorSelection = localSystem.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
  remoteActorSelection ! "hello form the \"local\" jvm"

  //Method 2: Resolve the actor selection to an actor ref
  import localSystem.dispatcher
  implicit val timeout = Timeout(3 seconds)
  val remoteActorRefFuture = remoteActorSelection.resolveOne()
  remoteActorRefFuture.onComplete{
    case Success(actorRef) => actorRef ! "I've resolved you in a future"
    case Failure(exception) => println(s"I've failed to resolve the actor because: $exception")
  }

  //Method 3: Actor identification via messages
  /*
  -actor resolved will ask for an actor selection from local actor system
  actor resolver will send a Identify(42) to the actor selection
  the remote actor will automatically with ActorIdentify(42, actorRef)
   */

  class ActorResolver extends Actor with ActorLogging{
    override def preStart(): Unit = {
      val remoteActorSelection = localSystem.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
      remoteActorSelection ! Identify(42)
    }
    override def receive: Receive = {
      case ActorIdentity(42, Some(actorRef)) =>
        actorRef ! "Thank you for identifying yourself"
    }
  }

  localSystem.actorOf(Props[ActorResolver], "localActorResolver")
}

object RemoteActorsDemo_Remote extends App {

  //two different actor system from different app running on same jvm exposing different ports
  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("remoting/remoteActors.conf").getConfig("remoteSystem"))

  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor],"remoteSimpleActor")

  remoteSimpleActor ! "hello, remote actor"
}
