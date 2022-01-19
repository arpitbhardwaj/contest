package com.ab
package persistence

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

import java.util.Date

object PersistentActorDemo extends App {

  //Command
  case class Invoice(recipient: String, date:Date, amount:Int)

  //Events
  case class InvoiceRecorded(id:Int,recipient: String, date:Date, amount:Int)

  class Accountant extends PersistentActor with ActorLogging {
    var latestInvoiceId = 0
    var totalAmount = 0

    //best practice : make it unique
    override def persistenceId: String = "simple-accountant"

    //normal receive method
    override def receiveCommand: Receive = {
      case Invoice(recipient,date,amount) =>
        /*
        When you receive a command
          1) you create an event to persist in store
          2) you persist the event, the pass in a callback that will get triggered once the event is wrotten in store
         */
        log.info(s"Receive invoice amount for: $amount")
        persist(InvoiceRecorded(latestInvoiceId,recipient, date, amount)){
          e =>
            latestInvoiceId += 1
            totalAmount += amount
            log.info(s"Persisted $e as invoice #${e.id}, for total amount of $totalAmount")
        }
    }

    //handler that will be called on recovery
    override def receiveRecover: Receive = {
      case InvoiceRecorded(id,_,_,amount) =>
        latestInvoiceId = id
        totalAmount = amount
        log.info(s"Recovered invoice #$id, for amount $totalAmount")
    }

  }

  val system = ActorSystem("persistentActors")
  val accountant = system.actorOf(Props[Accountant],"accountant")
  /*for (i <- 1 to 10){
    accountant ! Invoice("The MII Company", new Date, i*1000)
  }*/
}
