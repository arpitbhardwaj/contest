package com.ab
package highlevelserverapi

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer

object ActionableDirectives extends App {
  implicit val system = ActorSystem("CompositeDirectives")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives
  import akka.http.scaladsl.server.Directives._

  val completeOkRoute = complete(StatusCodes.OK)

  val failedRoute =
    path("notSupported"){
      failWith(new RuntimeException("Unsupported"))
    }

  val rejectRoute =
    path("home"){
      reject
    } ~
      path("index"){
        completeOkRoute
      }
}
