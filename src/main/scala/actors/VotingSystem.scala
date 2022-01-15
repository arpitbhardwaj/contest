package com.ab
package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import java.util.Optional

object VotingSystem extends App{

  case class Vote(candidate:String)
  case object VoteRequest
  case class VoteReply(candidate: Option[String])

  //stateful
  class Citizen extends Actor{
    var candidate:Option[String] = None
    override def receive: Receive = {
      case Vote(c) => candidate = Some(c)
      case VoteRequest => sender() ! VoteReply(candidate)
    }
  }

  //stateless
  class StatelessCitizen extends Actor{
    override def receive: Receive = {
      case Vote(c) => context.become(voted(c))
      //case VoteRequest => sender() ! VoteReply(None)
    }

    def voted(candidate:String): Receive = {
      case VoteRequest => sender() ! VoteReply(Some(candidate))
    }
  }



  case class AggregateVotes(citizens: Set[ActorRef])
  //stateful
  class VoteAggregator extends Actor{
    var stillWaiting:Set[ActorRef] = Set()
    var stats: Map[String,Int] = Map()
    override def receive: Receive = {
      case AggregateVotes(citizens) =>
        stillWaiting = citizens
        citizens.foreach(citizenRef => citizenRef ! VoteRequest)
      case VoteReply(None) =>
        //a citizen hasn't voted yet
        sender() ! VoteRequest
      case VoteReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        stats = stats + (candidate -> (stats.getOrElse(candidate,0)+1))
        if(newStillWaiting.isEmpty)
          println(s"poll stats: $stats")
        else
          stillWaiting = newStillWaiting
    }
  }

  //stateless
  class StatelessVoteAggregator extends Actor{

    override def receive: Receive = awaitingCommand

    def awaitingCommand:Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(citizenRef => citizenRef ! VoteRequest)
        context.become(awaitingStatus(citizens,Map()))
    }
    def awaitingStatus(stillWaiting: Set[ActorRef], stats: Map[String, Int]): Receive = {
      case VoteReply(None) =>
        //a citizen hasn't voted yet
        sender() ! VoteRequest
      case VoteReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val newStats = stats + (candidate -> (stats.getOrElse(candidate,0)+1))
        if(newStillWaiting.isEmpty)
          println(s"poll stats: $newStats")
        else
          context.become(awaitingStatus(newStillWaiting,newStats))
    }
  }
  val system = ActorSystem("votingSystem")
  val aish = system.actorOf(Props[Citizen])
  val abhi = system.actorOf(Props[Citizen])
  val ami = system.actorOf(Props[Citizen])
  val jaya = system.actorOf(Props[Citizen])

  aish ! Vote("Namo")
  abhi ! Vote("Namo")
  ami ! Vote("Raga")
  jaya ! Vote("Udhav")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(aish,abhi,ami,jaya))

  val sunny = system.actorOf(Props[StatelessCitizen])
  val bobby = system.actorOf(Props[StatelessCitizen])
  val dharm = system.actorOf(Props[StatelessCitizen])
  val hema = system.actorOf(Props[StatelessCitizen])

  sunny ! Vote("Namo")
  bobby ! Vote("Raga")
  dharm ! Vote("Raga")
  hema ! Vote("Udhav")

  val statelessVoteAggregator = system.actorOf(Props[StatelessVoteAggregator])
  statelessVoteAggregator ! AggregateVotes(Set(sunny,bobby,dharm,hema))

}
