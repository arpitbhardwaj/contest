package com.ab
package clientapi

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.ab.clientapi.PaymentSystemDomain.PaymentRequest
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

case class CreditCard(serialNum: String, secCode: String, acc:String)

object PaymentSystemDomain{
  case class PaymentRequest(creditCard: CreditCard, receiverAccount:String, amount:Double)
  case object PaymentAccepted
  case object PaymentRejected
}

trait PaymentJsonProtocol extends DefaultJsonProtocol{
  implicit val creditCardFormat = jsonFormat3(CreditCard)
  implicit val paymentRequestFormat = jsonFormat3(PaymentRequest)
}
class PaymentValidator extends Actor with ActorLogging{
  import PaymentSystemDomain._

  override def receive: Receive = {
    case PaymentRequest(CreditCard(serialNum, _, senderAccount), receiverAccount, amount) =>
      log.info(s"$senderAccount is trying to send $amount dollars to $receiverAccount")
      if (serialNum == "1234-1234-1234-1234")
        sender() ! PaymentRejected
      else
        sender() ! PaymentAccepted
  }
}
object PaymentSystem extends App
  with PaymentJsonProtocol
  with SprayJsonSupport
  {

  implicit val system = ActorSystem("PaymentSystem")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives
  import akka.http.scaladsl.server.Directives._
    import PaymentSystemDomain._

  val paymentActor = system.actorOf(Props[PaymentValidator], "PaymentValidator")
  implicit val timeout = Timeout(2 seconds)

  val paymentRoute =
    path("api" / "payments"){
      post{
        entity(as[PaymentRequest]){
          paymentReq =>
          val responseFuture = (paymentActor ? paymentReq).map{
            case PaymentRejected => StatusCodes.Forbidden
            case PaymentAccepted => StatusCodes.OK
            case _ => StatusCodes.BadRequest
          }
            complete(responseFuture)
        }
      }
    }

    Http().bindAndHandle(paymentRoute, "localhost", 8080)
}
