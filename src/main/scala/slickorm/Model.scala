package com.ab
package slickorm

import java.time.LocalDate


case class Movie(id: Long,
                 name: String,
                 releaseDate: LocalDate,
                 lengthInMin: Int)

case class Actor(id: Long,
                 name: String)

object SlickTables {
  import slick.jdbc.PostgresProfile.api._
  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies"), "Movie") {
    def id = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def releaseDate = column[LocalDate]("release_date")
    def lengthInMin = column[Int]("length_in_min")

    def * = (id, name, releaseDate, lengthInMin) <> (Movie.tupled, Movie.unapply)
  }

  lazy val movieTable = TableQuery[MovieTable]

  class ActorTable(tag: Tag) extends Table[Actor](tag, Some("movies"), "Actor") {
    def id = column[Long]("actor_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")

    def * = (id, name)<> (Actor.tupled, Actor.unapply)
  }

  lazy val actorTable = TableQuery[ActorTable]

}
