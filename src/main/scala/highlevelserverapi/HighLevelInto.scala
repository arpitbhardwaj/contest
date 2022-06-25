package com.ab
package highlevelserverapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

/**
 *
 * A RequestContext contains (The DS handled by the route)
 *  the HttpRequest being handled
 *  the actor system, materialiazer
 *  logging adapter
 *  routing settings
 *  etc
 */
object HighLevelInto extends App {
  implicit val system = ActorSystem("HighLevelIntro")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives
  import akka.http.scaladsl.server.Directives._

  val simpleRoute: Route =
    path("home") {    //Directive
      complete(StatusCodes.OK)  //Directive
    }

  val pathGetRoute:Route =
    path("home") {
      get{
        complete(StatusCodes.OK)
      }
    }

  //chaining directive with ~
  val chainedRoutes:Route = {
    path("myEndpoint"){
      get{
        complete(StatusCodes.OK)
      } ~ // without this the code compiles but it only the last directive will be used and others are ignored
        post{
          complete(StatusCodes.Forbidden)
        }
    } ~
      path("home"){
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            """
              |<html>
              |<body>
              | Hello from the hight level
              | </body>
              | </html>
              |""".stripMargin
          )
        )
      }
  } // routing tree

  //Http().bindAndHandle(simpleRoute, "localhost", 8080)
  //Http().bindAndHandle(pathGetRoute, "localhost", 8080)
  Http().bindAndHandle(chainedRoutes, "localhost", 8080)

}
