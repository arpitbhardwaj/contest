package com.ab
package remoting

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorSystem, Identify, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * @author Arpit Bhardwaj
 *
 * Method 1: Actor Selection
 * Method 2: Resolve the actor selection to an actor ref
 * Method 3: Actor identification via messages
 *          - actor resolved will ask for an actor selection from local actor system
 *          - actor resolver will send a Identify(42) to the actor selection
 *          - the remote actor will automatically respond with ActorIdentify(42, actorRef)
 *
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

  //Method 1
  val remoteActorSelection = localSystem.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
  remoteActorSelection ! "hello form the \"local\" jvm"

  //Method 2
  import localSystem.dispatcher
  implicit val timeout = Timeout(3 seconds)
  val remoteActorRefFuture = remoteActorSelection.resolveOne()
  remoteActorRefFuture.onComplete{
    case Success(actorRef) => actorRef ! "I've resolved you in a future"
    case Failure(exception) => println(s"I've failed to resolve the actor because: $exception")
  }

  //Method 3
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
  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("remoting/remoteActors.conf")
    .getConfig("remoteSystem"))
  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor],"remoteSimpleActor")
  remoteSimpleActor ! "hello, remote actor"
}
