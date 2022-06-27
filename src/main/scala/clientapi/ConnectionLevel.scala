package com.ab
package clientapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import spray.json._

import scala.util.{Failure, Success}

object ConnectionLevel extends App with PaymentJsonProtocol {
  implicit val system = ActorSystem("ConnectionLevel")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val connectionFlow = Http().outgoingConnection("www.google.com")

  def oneOfRequest(req: HttpRequest) =
    Source.single(req).via(connectionFlow).runWith(Sink.head)

  /*oneOfRequest(HttpRequest()).onComplete{
    case Success(res) => println(s"Successful response $res")
    case Failure(exception) => println(s"Failed with exception $exception")
  }*/

  /*
  A small payment system
   */

  import PaymentSystemDomain._
  val creditCardList = List(
    CreditCard("8789-7465-9898-2534","568", "hda-78-757-jh76"),
    CreditCard("1234-1234-1234-1234", "343","ts76-78-757-jh76"),
    CreditCard("4786-7465-9898-2534","908", "owqu-78-757-jh76")
  )

  val paymentReqs = creditCardList.map(creditCard => PaymentRequest(creditCard, "myStore", 99))
  val httpReq = paymentReqs.map(paymentReq =>
    HttpRequest(
      HttpMethods.POST,
      uri = Uri("/api/payments"),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        paymentReq.toJson.prettyPrint
      )
    )
  )

  Source(httpReq)
    .via(Http().outgoingConnection("localhost",8080))
    .to(Sink.foreach[HttpResponse](println))
    .run()


}
