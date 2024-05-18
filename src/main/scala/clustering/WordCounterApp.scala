package com.ab
package clustering

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Props, ReceiveTimeout}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import akka.pattern.pipe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps
import scala.util.Random

object WordCounterDomain {
  case class ProcessFile(fileName: String)
  case class WordCountTask(text: String, agreement: ActorRef)
  case class WordCountResult(count: Int)
}

/*
  Identify the workers in the remote JVM
     - create actor selection for every worker from 1 to nWorkers
     - send identify messages to the actor selection
     - get into an initialization state, while you are receiving actor identities
 */
class WordCountMaster extends Actor with ActorLogging{
  import WordCounterDomain._

  import context.dispatcher
  implicit val timeout = Timeout(3 seconds)

  val cluster = Cluster(context.system)
  var workers: Map[Address, ActorRef] = Map.empty
  var pendingRemoval: Map[Address, ActorRef] = Map.empty

  override def preStart(): Unit = {
    cluster.subscribe(
      self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember]
    )
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }
  override def receive: Receive = handleClusterEvents.
    orElse(handleWorkerRegistration).orElse(handleJob)

  def handleClusterEvents: Receive = {
    case MemberUp(member) if member.hasRole("worker") =>
      log.info(s"Member is up: ${member.address}")
      if(pendingRemoval.contains(member.address)){
        pendingRemoval = pendingRemoval - member.address
      } else {
        val workerSelection = context.actorSelection(s"${member.address}/user/wordCountWorker")
        workerSelection.resolveOne().map(ref => (member.address, ref)).pipeTo(self)
      }
    case MemberRemoved(member, previousStatus) if member.hasRole("worker")=>
      log.info(s"Member ${member.address} removed after $previousStatus")
      workers = workers - member.address
    case UnreachableMember(member) =>
      log.info(s"Member ${member.address} detected as unreachable")
      val workerOption = workers.get(member.address)
      workerOption.foreach(ref => pendingRemoval + (member.address -> ref))
    case m: MemberEvent =>
      log.info(s"Another member event which i don't care: $m")
  }

  def handleWorkerRegistration: Receive = {
    case pair:(Address, ActorRef) =>
      log.info(s"Registering Worker: $pair")
      workers = workers + pair
  }

  def handleJob: Receive = {
    case ProcessFile(fileName) =>
      val aggregator = context.actorOf(Props[Aggregator], "aggregator")
      scala.io.Source.fromFile("src/main/resources/txt/lipsum.txt").getLines().foreach{
        line =>
          val workerIndex = Random.nextInt((workers--pendingRemoval.keys).size)
          val worker = (workers--pendingRemoval.keys).values.toSeq(workerIndex)
          worker ! WordCountTask(line, aggregator)
      }
  }
}

class Aggregator extends Actor with ActorLogging{
  import WordCounterDomain._
  context.setReceiveTimeout(3 seconds)
  override def receive: Receive = online(0)

  def online(totalCount: Int): Receive = {
    case WordCountResult(count) =>
      context.become(online(totalCount + count))
    case ReceiveTimeout =>
      log.info(s"Total count: $totalCount")
      context.setReceiveTimeout(Duration.Undefined)
  }
}

class WordCountWorker extends Actor with ActorLogging{
  import WordCounterDomain._

  override def receive: Receive = {
    case WordCountTask(text, aggregator) =>
      log.info(s"i'm processing : $text")
      aggregator ! WordCountResult(text.split(" ").length)
  }
}

object SeedNodes extends App {
  import WordCounterDomain._
  def createNode(port: Int, role: String, props: Props, actorName: String): ActorRef  = {
    val config = ConfigFactory.parseString(
      s"""
        |akka.remote.artery.canonical.port = $port
        |akka.cluster.roles = [$role]
        |""".stripMargin
    ).withFallback(ConfigFactory.load("clustering/wordCounterApp.conf"))

    val system = ActorSystem("AbixelCluster", config)
    system.actorOf(props, actorName)
  }

  val master = createNode(2551, "master", Props[WordCountMaster], "wordCountMaster")
  createNode(2552, "worker", Props[WordCountWorker], "wordCountWorker")
  createNode(2553, "worker", Props[WordCountWorker], "wordCountWorker")

  Thread.sleep(10000)
  master ! ProcessFile("src/main/resources/txt/lipsum.txt")
}




