package com.ab
package highlevelserverapi

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
//Step 1
import spray.json._
case class Player(name:String, character:String, level:Int)

object GameAreaMap{
  case object GetAllPlayers
  case class GetPlayer(name:String)
  case class GetPlayerByClass(character:String)
  case class AddPlayer(player:Player)
  case class RemovePlayer(player:Player)
  case object OperationSuccess
}
class GameAreaMap extends Actor with ActorLogging{
  import GameAreaMap._
  var players = Map[String, Player]()

  override def receive: Receive = {
    case GetAllPlayers =>
      log.info("Getting All Players")
      sender() ! players.values.toList

    case GetPlayer(name) =>
      log.info(s"Getting player with name: $name")
      sender() ! players.get(name)

    case GetPlayerByClass(character) =>
      log.info(s"Getting All Players with a character class: $character")
      sender() ! players.values.toList.filter(_.character == character)

    case AddPlayer(player) =>
      log.info(s"Adding Player $player")
      players = players + (player.name -> player)
      sender() ! OperationSuccess

    case RemovePlayer(player) =>
      log.info(s"Removing Player $player")
      players = players - player.name
      sender() ! OperationSuccess

  }
}

//step 2
trait PlayerJsonProtocol extends DefaultJsonProtocol{
  implicit val playerFormat = jsonFormat3(Player)
}


object MarshallingJson extends App
  //step 3
  with PlayerJsonProtocol
  //step 4
  with SprayJsonSupport
  {
  implicit val system = ActorSystem("HighLevelIntro")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives
  import akka.http.scaladsl.server.Directives._

  import GameAreaMap._
  val gameMapActor = system.actorOf(Props[GameAreaMap],"GameAreaMap")

  val playersList = List(
    Player("Arpit", "Warrior", 31),
    Player("Joker", "Elf", 29),
    Player("Mocha", "Warrior", 34)
  )

  playersList.foreach{
    player => gameMapActor ! AddPlayer(player)
  }

  /*
  - GET /api/player
  - GET /api/player/name
  - GET /api/player?name=X
  - GET /api/player/class/(charclass)
  - POST /api/player
  - DELETE /api/player
   */


  implicit val timeout = Timeout(2 seconds)
  val gameRoute: Route =
    pathPrefix("api" / "player"){
      get{
        path("class" / Segment){
          character =>
            val playerFuture = (gameMapActor ?GetPlayerByClass(character)).mapTo[List[Player]]
            complete(playerFuture)
        } ~
          (path(Segment) | parameter("name")){
            name =>
              val playerOptionFuture = (gameMapActor ?GetPlayer(name)).mapTo[Option[Player]]
              complete(playerOptionFuture)
          } ~
          pathEndOrSingleSlash{
            complete((gameMapActor ? GetAllPlayers).mapTo[List[Player]])
          }
      } ~
        post{
          entity(as[Player]) {
            player =>
              complete((gameMapActor ? AddPlayer(player)).map(_ => StatusCodes.OK))
          }
        } ~
        delete{
          entity(as[Player]) {
            player =>
              complete((gameMapActor ? RemovePlayer(player)).map(_ => StatusCodes.OK))
          }
        }
    }

    Http().bindAndHandle(gameRoute, "localhost", 8080)
}
