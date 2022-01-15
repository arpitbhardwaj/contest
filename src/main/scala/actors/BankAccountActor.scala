package com.ab
package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.ab.actors.BankAccountActor.Person.LiveTheLife

/**
 * Bank Account Actor
 *    receives
 *      -Deposit an amount
 *      -Withdraw an amount
 *      -Statement
 *    replies
 *      - Success
 *      - Failure
 *
 * Interact via another actor
 */
object BankAccountActor extends App {

  object BankAccount{
    case class Deposit(amt:Int)
    case class Withdraw(amt:Int)
    case object Statement
    case class TransactionSuccess(msg:String)
    case class TransactionFailure(msg:String)
  }

  class BankAccount extends Actor{
    import BankAccount._
    var balance = 0
    override def receive: Receive = {
      case Deposit(amt) =>
        if(amt < 0)
          sender() ! TransactionFailure("invalid deposit amount")
        else {
          balance += amt
          sender() ! TransactionSuccess(s"Successfully deposited $amt")
        }
      case Withdraw(amt) =>
        if (amt < 0)
          sender() ! TransactionFailure("invalid withdraw amount")
        else if( amt > balance )
          sender() ! TransactionFailure("insufficient balance")
        else {
          balance -= amt
          sender() ! TransactionSuccess(s"Successfully withdraw $amt")
        }
      case Statement => sender() ! s"Your balance is $balance"
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }
  class Person extends Actor{
    import Person._
    import BankAccount._
    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(5000)
        account ! Statement
      case msg => println(msg.toString)
    }
  }

  val actorSystem = ActorSystem("firstActorSystem")
  val bankAccountActor = actorSystem.actorOf(Props[BankAccount], "bankAccountActor")
  val personActor = actorSystem.actorOf(Props[Person], "personActor")
  personActor ! LiveTheLife(bankAccountActor)
}
