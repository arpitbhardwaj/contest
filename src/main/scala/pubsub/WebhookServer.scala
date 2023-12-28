package com.ab
package pubsub

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Sink
import akka.util.ByteString

import scala.concurrent.Future

object WebhookServer extends App {

  implicit val system = ActorSystem("WebhookServerAS")
  import system.dispatcher

  import akka.http.scaladsl.server.Directives._

  private val simplePostRoute:Route = {
    path("webhook"){
      extractRequestEntity {
        (entity: HttpEntity) =>
          val source = entity.getDataBytes
          val sink: Sink[ByteString, Future[ByteString]] =
            Sink.fold[ByteString, ByteString](ByteString.empty)((x, y) => x ++ y)
          source.runWith(sink, system) map { byteString =>
            if (byteString.nonEmpty)
              println(s"Received ${byteString.map(_.toChar).mkString}")
            else
              println("Empty body")
          }
          complete(StatusCodes.OK)
      }
    }
  }

  Http().newServerAt("localhost", 8080).bind(simplePostRoute)
}
