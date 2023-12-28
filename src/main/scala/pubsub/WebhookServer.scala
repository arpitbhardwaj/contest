package com.ab
package pubsub

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object WebhookServer extends App {

  implicit val system = ActorSystem("WebhookServerAS")
  implicit val materializer = ActorMaterializer()

  import akka.http.scaladsl.server.Directives._

  val chainedRoutes:Route = {
    path("webhook"){
      post{
          complete(StatusCodes.OK)
        }
    }
  }

  Http().bindAndHandle(chainedRoutes, "localhost", 8080)
}
