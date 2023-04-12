package com.ab
package highlevelserverapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object CompositeDirectives extends App {

  implicit val system = ActorSystem("CompositeDirectives")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives
  import akka.http.scaladsl.server.Directives._

  val simpleNestedRoute:Route =
    path("api" / "item") {
      get{
        complete(StatusCodes.OK)
      }
    }

  //as both are filtering directives
  val compositeSimpleNestedRoute:Route = (path("api" / "item") & get){
    complete(StatusCodes.OK)
  }

  val compositeExtractRequestRoute:Route =
    (path("controlEndpoint") & extractRequest & extractLog){
      (request, log) =>
        log.info(s"I got $request")
        complete(StatusCodes.OK)
    }

  // /about and /aboutUs
  val repeatedRoute:Route ={
    path("about") {
      complete(StatusCodes.OK)
    } ~
      path("aboutUs") {
        complete(StatusCodes.OK)
      }
  }

  val compositeRoute: Route =
    (path("about") | path("aboutUs")){
      complete(StatusCodes.OK)
    }

  //blog.com/42 AND blog.com?postId=42
  val blogById:Route =
    path(IntNumber){
      (postId: Int) =>
        println(s"i have got a number in my path $postId")
        complete(StatusCodes.OK)
    }

  val blogByQueryParam:Route =
    parameter("id".as[Int]){
      (postId:Int) =>
        println(s"i have got a number in my path $postId")
        complete(StatusCodes.OK)
    }

  val compositeBlogById: Route =
    (path(IntNumber) | parameter("id".as[Int])){
      (postId: Int) =>
      complete(StatusCodes.OK)
    }

  Http().bindAndHandle(simpleNestedRoute, "localhost", 8080)

}
