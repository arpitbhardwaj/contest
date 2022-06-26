package com.ab
package highlevelserverapi

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{MethodRejection, MissingQueryParamRejection, Rejection, RejectionHandler, Route}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer

/**
 * If a request doesn't match a filter directive, its rejected
 * reject = pass the request to another branch in the routing tree
 * a rejection is not a failure
 *
 * Rejections are aggregated
 */
object HandlingRejections extends App {

  implicit val system = ActorSystem("HandlingRejections")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives
  import akka.http.scaladsl.server.Directives._

  val simpleRoute: Route =
    path("api" / "myEndpoint"){
      get{
        complete(StatusCodes.OK)
      }
    }

  val badRequestHandler:RejectionHandler = {
    rejections:Seq[Rejection] =>
      println(s"i have encountered rejections $rejections")
      Some(complete(StatusCodes.BadRequest))
  }

  val forbiddenHandler:RejectionHandler = {
    rejections:Seq[Rejection] =>
      println(s"i have encountered rejections $rejections")
      Some(complete(StatusCodes.Forbidden))
  }

  val simpleRouteWithHandler =
    handleRejections(badRequestHandler) {
      get{
        complete(StatusCodes.OK)
      } ~
        post{
          handleRejections(forbiddenHandler){
            parameter("myParam"){
              _ =>
                complete(StatusCodes.OK)
            }
          }
        }
    }

  Http().bindAndHandle(simpleRouteWithHandler, "localhost", 8080)

  implicit val customRejectionHandler = RejectionHandler.newBuilder()
    .handle{
      case m:MethodRejection =>
        println(s"i have got a method rejection: ${m}")
        complete("Rejected Method")
    }
    .handle{
      case m:MissingQueryParamRejection =>
        println(s"i have got a query param rejection: ${m}")
        complete("Rejected QueryParam")
    }
    .result()

  Http().bindAndHandle(simpleRoute, "localhost", 8080)
}
