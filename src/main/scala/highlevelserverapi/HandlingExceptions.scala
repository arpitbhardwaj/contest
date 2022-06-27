package com.ab
package highlevelserverapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer

object HandlingExceptions extends App {
  implicit val system = ActorSystem("HandlingExceptions")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives
  import akka.http.scaladsl.server.Directives._

  val simpleRoute: Route =
    path("api" / "people"){
      get{
        throw new RuntimeException("its taking too long")
      } ~
        post{
          parameter("id"){
            id =>
              if (id.length > 2) {
                throw new NoSuchElementException(s"Parameter ${id} can not be found in the database")
              }
              complete(StatusCodes.OK)
          }
        }
    }

  implicit val customExceptionHandler:ExceptionHandler = ExceptionHandler{
    case e:RuntimeException =>
      complete(StatusCodes.NotFound, e.getMessage)
    case e:IllegalArgumentException =>
      complete(StatusCodes.BadRequest, e.getMessage)
  }

  //Http().bindAndHandle(simpleRoute, "localhost", 8080)

  val runtimeExceptionHandler:ExceptionHandler = ExceptionHandler {
    case e: RuntimeException =>
      complete(StatusCodes.NotFound, e.getMessage)
  }

  val illegalExceptionHandler:ExceptionHandler = ExceptionHandler {
    case e:IllegalArgumentException =>
      complete(StatusCodes.BadRequest, e.getMessage)
  }

  val delicateRoute: Route = {
    handleExceptions(runtimeExceptionHandler){
      path("api" / "people"){
        get{
          throw new RuntimeException("its taking too long")
        } ~
          handleExceptions(illegalExceptionHandler){
            post{
              parameter("id"){
                id =>
                  if (id.length > 2)
                    throw new NoSuchElementException(s"Parameter ${id} can not be found in the database")
                  complete(StatusCodes.OK)
              }
            }
          }
        }
      }
    }

  Http().bindAndHandle(delicateRoute, "localhost", 8080)
}
