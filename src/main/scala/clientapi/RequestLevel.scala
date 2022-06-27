package com.ab
package clientapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import spray.json._

import java.util.UUID
import scala.util.{Failure, Success}

/**
 *
 *
 */
object RequestLevel extends App with PaymentJsonProtocol{

  implicit val system = ActorSystem("RequestLevel")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val responseFuture = Http().singleRequest(HttpRequest(uri = "http://www.google.com"))
  responseFuture.onComplete {
    case Success(res) =>
      res.discardEntityBytes()
      println(s"request is successfull and returned: $res")
    case Failure(exception) =>
      println(s"The request failed with $exception")
  }


  import PaymentSystemDomain._
  val creditCardList = List(
    CreditCard("8789-7465-9898-2534","568", "hda-78-757-jh76"),
    CreditCard("1234-1234-1234-1234", "343","ts76-78-757-jh76"),
    CreditCard("4786-7465-9898-2534","908", "owqu-78-757-jh76")
  )

  val paymentReqs = creditCardList.map(creditCard => PaymentRequest(creditCard, "myStore", 99))
  val httpReq = paymentReqs.map(paymentReq =>
    (
      HttpRequest(
        HttpMethods.POST,
        uri = "http://localhost:8080/api/payments",
        entity = HttpEntity(
          ContentTypes.`application/json`,
          paymentReq.toJson.prettyPrint
        )
      )
    )
  )

  Source(httpReq)
    .mapAsync(10)(req => Http().singleRequest(req))
    .runForeach(println)

  Source(httpReq)
    .mapAsyncUnordered(10)(req => Http().singleRequest(req))
    .runForeach(println)
}
