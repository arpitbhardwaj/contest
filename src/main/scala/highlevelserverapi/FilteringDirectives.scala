package com.ab
package highlevelserverapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

/**
 *
 * Directive creates routes; composing routes creates routing tree
 *  filtering and nesting
 *  chaining with ~
 *  extracting data
 *
 * What a route can do with a RequestContext
 *  complete it synchronously with a response
 *  complete it asynchronously with a future(response)
 *  handle it asynchronously by returning a source (advanced)
 *  reject and pass it to the next route
 *  fail it
 *
 */
class FilteringDirectives extends App {
  implicit val system = ActorSystem("FilteringDirectives")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives
  import akka.http.scaladsl.server.Directives._

  /**
   * filtering directives
   */

  val simpleMethodRoute:Route =
    post{ // directives for get, put, patch, delete, head, options
      complete(StatusCodes.Forbidden)
    }

  val simplePathRoute: Route =
    path("about") {
      complete(
        HttpEntity(
          ContentTypes.`application/json`,
          """
            |<html>
            |<body>
            | About Page
            | </body>
            | </html>
            |""".stripMargin
        )
      )
    }

  val complexPathRoute:Route =
    path("api" / "myEndpoint"){
      complete(StatusCodes.OK)
    }

  //completely different here / will be url encoded
  val complexPathRoute2:Route =
    path("api/myEndpoint"){
      complete(StatusCodes.OK)
    }

  val pathEndRoute:Route =
    pathEndOrSingleSlash{ //localhost:8080
      complete(StatusCodes.OK)
    }

  Http().bindAndHandle(complexPathRoute2, "localhost", 8080)
}
