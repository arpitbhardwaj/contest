package com.ab
package clientapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import spray.json._

import java.util.UUID
import scala.util.{Failure, Success}


/**
 *
 * Benefits
 * the freedom from managing individual connections
 * the ability to attach data to request (aside form payloads)
 *
 * Recommended for
 * High Volume and low latency requests and Short lived request
 *
 * Do Not use it for
 * one-off requests (use the request level api)
 * long lived requests (use the connection level api)
 */
object HostLevel extends App with PaymentJsonProtocol{
  implicit val system = ActorSystem("HostLevel")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val poolFlow = Http().cachedHostConnectionPool[Int]("www.google.com")

  Source(1 to 10)
    .map(i => (HttpRequest(),i))
    .via(poolFlow)
    .map {
      case (Success(response), value) =>
        response.discardEntityBytes() //Very important else leaking connection happen
        s"Request $value has received response: $response"
      case (Failure(ex), value) =>
        s"Request $value has been failed: $ex"
    }
    .runWith(Sink.foreach[String](println))

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
        uri = Uri("/api/payments"),
        entity = HttpEntity(
          ContentTypes.`application/json`,
          paymentReq.toJson.prettyPrint
        )
      ),
      UUID.randomUUID().toString
    )
  )

  Source(httpReq)
    .via(Http().cachedHostConnectionPool[String]("localhost",8080))
    .runForeach {
      case (Success(response), orderId) =>
        //response.discardEntityBytes() //Very important else leaking connection happen
        s"Request $orderId has received response: $response"
      case (Failure(ex), orderId) =>
        s"Request $orderId has been failed: $ex"
      case (Success(response@HttpResponse(StatusCodes.Forbidden, _, _, _)), orderId) =>
        s"Request $orderId is not allowed to proceed: $response"
    }
}
