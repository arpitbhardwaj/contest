package com.ab
package remoting

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, ActorSystem, Identify, PoisonPill, Props}
import com.typesafe.config.ConfigFactory

object WordCounterDomain {
  case class Initialize(nWorkers: Int)
  case class WordCountTask(text: String)
  case class WordCountResult(count: Int)
  case object EndWordCount
}

/*
  Identify the workers in the remote JVM
     - create actor selection for every worker from 1 to nWorkers
     - send identify messages to the actor selection
     - get into an initialization state, while you are receiving actor identities
 */
class WordCountMaster extends Actor with ActorLogging{
  import WordCounterDomain._

  override def receive: Receive = {
    case Initialize(nWorkers) =>
      log.info("Master initializing...")
      val workerSelections = (1 to nWorkers).map(id => context.actorSelection(s"akka://WorkerSystem@localhost:2552/user/wordCountWorker$id"))
      workerSelections.foreach(_ ! Identify("42"))
      context.become(initializing(List(), nWorkers))
    }

    private def initializing(workers: List[ActorRef], remainingWorkers:Int):Receive ={
      case ActorIdentity("42", Some(workerRef)) =>
        log.info(s"Worker Identified: $workerRef")
        if (remainingWorkers == 1)
          context.become(online(workerRef :: workers, 0,0))
        else
          context.become(initializing(workerRef::workers, remainingWorkers-1))
    }

    def online(workers: List[ActorRef], remainingTasks: Int, totalCount: Int): Receive = {
      case text: String =>
        val sentences = text.split("\\. ")  //split into sentences
        //send sentences to workers in turn
        Iterator.continually(workers).flatten.zip(sentences.iterator).foreach{
          pair =>
            val (worker, sentence) = pair
            worker ! WordCountTask(sentence)
        }
        context.become(online(workers, remainingTasks + sentences.length, totalCount))

      case WordCountResult(count) =>
        if (remainingTasks == 1){
          log.info(s"Total Count: ${totalCount + count}")
          workers.foreach(_ ! PoisonPill)
          context.stop(self)
        } else{
          context.become(online(workers, remainingTasks - 1 , totalCount + count))
        }
    }
}

class WordCountWorker extends Actor with ActorLogging{
  import WordCounterDomain._

  override def receive: Receive = {
    case WordCountTask(text) =>
      log.info(s"i'm processing : $text")
      sender() ! WordCountResult(text.split(" ").length)
  }
}

object MasterApp extends App{
  import WordCounterDomain._
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2551
      |""".stripMargin
  ).withFallback(ConfigFactory.load("remoting/wordCounterApp.conf"))

  val system = ActorSystem("MasterSystem", config)

  val master = system.actorOf(Props[WordCountMaster], "wordCountMaster")
  master ! Initialize(5)
  Thread.sleep(1000)

  scala.io.Source.fromFile("src/main/resources/txt/lipsum.txt").getLines().foreach{
    line =>
      master ! line
  }
}

object WorkerApp extends App{
  import WordCounterDomain._
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2552
      |""".stripMargin
  ).withFallback(ConfigFactory.load("remoting/wordCounterApp.conf"))

  val system = ActorSystem("WorkerSystem", config)
  (1 to 5).map(i => system.actorOf(Props[WordCountWorker], s"wordCountWorker$i"))
}

