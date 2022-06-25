package com.ab
package highlevelserverapi

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.stream.ActorMaterializer

/**
 *
 * Symbols are automatically interned into the jvm
 *  offer performance benefits as they always compare by reference equality instead of content equality
 */
class ExtractionDirectives extends App {

  implicit val system = ActorSystem("ExtractionDirectives")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives
  import akka.http.scaladsl.server.Directives._

  val pathExtractionRoute:Route =
    path("api" / "item" / IntNumber){
      (itemNumber: Int) =>
        //other directives
        println(s"i have got a number in my path $itemNumber")
        complete(StatusCodes.OK)
    }

  val pathMultiExtractionRoute:Route =
    path("api" / "order" / IntNumber / IntNumber){
      (id, inventory) =>
        //other directives
        println(s"i have got a number in my path $id and $inventory")
        complete(StatusCodes.OK)
    }

  //extract query parameters
  ///api/item?id=45
  val queryParamExtractionRoute:Route =
    path("api" / "item"){
          parameter("id"){
            (itemId:String) =>
              //other directives
              println(s"i have got a number in my path $itemId")
              complete(StatusCodes.OK)
          }
    }

  val queryParamExtractionRoute2:Route =
    path("api" / "item"){
      parameter("id".as[Int]){
        (itemId:Int) =>
          //other directives
          println(s"i have got a number in my path $itemId")
          complete(StatusCodes.OK)
      }
    }

  val queryParamExtractionRoute3:Route =
    path("api" / "item"){
      parameter('id.as[Int]){ //synmbols
        (itemId:Int) =>
          //other directives
          println(s"i have go a number in my path $itemId")
          complete(StatusCodes.OK)
      }
    }

  val extractRequestRoute: Route =
    path("controlEndpoint"){

      extractRequest{
        (httpRequest: HttpRequest) =>
          extractLog{
            (log: LoggingAdapter) =>
              log.info(s"I got $httpRequest")
              complete(StatusCodes.OK)
          }
      }
    }

  Http().bindAndHandle(pathExtractionRoute, "localhost", 8080)
}
