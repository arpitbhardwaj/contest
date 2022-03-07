package com.ab
package remoting

import akka.actor.{Actor, ActorLogging, ActorSystem, Address, AddressFromURIString, Deploy, Props, Terminated}
import akka.dispatch.sysmsg.Terminate
import akka.remote.RemoteScope
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory

/**
 *
 * Steps:
 *  The name of the actor is checked in config for remote deployment
 *    if its not there it will created locally
 *  the props passed to actorOf will be sent to remote actor system
 *  the remote actor system will create it there
 *  an ActorRef is returned
 *
 *  Caveat: The props objects need to be serializable
 *    in 99% cases it is
 *    watch for lambdas that need to be sent over the wire
 *    the actor class needs to be in the remote JVM's classpath
 */
object DeployingActorsRemotely_LocalApp extends App {
  val system = ActorSystem("LocalActorSystem", ConfigFactory.load("remoting/deployingActorsRemotely.conf")
    .getConfig("localApp"))
  val remoteActor = system.actorOf(Props[SimpleActor],"remoteActor")
  remoteActor ! "hello, remote actor"

  //expected: akka://RemoteSystem@localhost:2552/user/remoteActor
  //actual:
  println(remoteActor)

  /*

  //programmatic remote deployment
  val remoteSystemAddress:Address = AddressFromURIString("akka://RemoteSystem@localhost:2552")
  val remotelyDeployedActor = system.actorOf(
    Props[SimpleActor].withDeploy(
      Deploy(scope = RemoteScope(remoteSystemAddress))
    )
  )
  remotelyDeployedActor ! "hi, remotely deployed actor"

  //routers with routes deployed remotely
  val poolRouter = system.actorOf(FromConfig.props(Props[SimpleActor]), "myRouterWithRemoteChildren")
  (1 to 10).map(i => s"message $i").foreach(poolRouter ! _)

  //watching remote actors
  class ParentActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case "create" =>
        log.info("Creating remote child")
        val child = context.actorOf(Props[SimpleActor], "remoteChild")
        context.watch(child)
      case Terminated(ref) =>
        log.warning(s"Child $ref terminated")
    }
  }

  val watcher = system.actorOf(Props[ParentActor], "watcher")
  watcher ! "create"

  Thread.sleep(1000)
  system.actorSelection()

   */
}


object DeployingActorsRemotely_RemoteApp extends App {
  val remoteSystem = ActorSystem("RemoteActorSystem", ConfigFactory.load("remoting/deployingActorsRemotely.conf")
    .getConfig("remoteApp"))
}