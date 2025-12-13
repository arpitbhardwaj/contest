package com.ab
package slickorm

import slick.jdbc.GetResult

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * @author Arpit Bhardwaj
 *
 */

object PrivateExecutionContext {
  private val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(executor)
}

object MainDemo {
  import slick.jdbc.PostgresProfile.api._
  import PrivateExecutionContext._

  val shawshank = Movie(1L, "Shawshank Redemption", LocalDate.of(1994, 4, 2), 162)
  val shallow = Movie(2L, "Shallow", LocalDate.of(2001, 4, 2), 132)
  val dhurandhar = Movie(2L, "Dhurandhar", LocalDate.of(2001, 4, 2), 132)
  val tomHanks = Actor(1l, "Tom Hanks")
  val juliaRoberts = Actor(2L, "Julia Roberts")
  val akshayKhanna = Actor(2L, "Akshay Khanna")

  private def insertMovie(movie: Movie): Unit = {
    val insertQuery = SlickTables.movieTable += movie
    val futureId = Connection.db.run(insertQuery)
    futureId.onComplete {
      case Failure(ex) => println(s"Query failed: $ex")
      case Success(_) => println(s"Query successful: Movie Inserted")
    }
  }

  private def insertMultipleActor(actors: Seq[Actor]): Unit = {
    val insertQuery = SlickTables.actorTable ++= actors
    val future = Connection.db.run(insertQuery)
    future.onComplete {
      case Failure(ex) => println(s"Query failed: $ex")
      case Success(_) => println(s"Query successful: Actors Inserted")
    }
  }

  private def readAllMovie(): Unit = {
    val readAllQuery = SlickTables.movieTable.result
    val result = Connection.db.run(readAllQuery)
    result.onComplete {
      case Failure(ex) => println(s"Query failed: $ex")
      case Success(movies) => println(s"Query successful: Movie List: ${movies.mkString(",")}")
    }
  }

  private def readSpecificMovie(): Unit = {
    val readSpecificQuery = SlickTables.movieTable.result
    val result = Connection.db.run(readSpecificQuery)
    result.onComplete {
      case Failure(ex) => println(s"Query failed: $ex")
      case Success(movies) => println(s"Query successful: Movie List: ${movies.mkString(",")}")
    }
  }

  private def readAllMoviesUsingPlainQuery(): Unit = {
    implicit val getResultMovie: GetResult[Movie] =
      GetResult(r => Movie(r.<<, r.<<, LocalDate.parse(r.nextString()), r.<<))
    val plainReadAllQuery = sql"""select * from movies."Movie"""".as[Movie]
    val result = Connection.db.run(plainReadAllQuery)
    result.onComplete {
      case Failure(ex) => println(s"Query failed: $ex")
      case Success(movies) => println(s"Query successful: Movie List: ${movies.mkString(",")}")
    }
  }

  private def runMultipleQueriesInSingleTransaction():Unit = {
    val insertMovieQuery = SlickTables.movieTable += dhurandhar
    val insertActorQuery = SlickTables.actorTable += akshayKhanna
    val finalInsertQuery = DBIO.seq(insertMovieQuery, insertActorQuery)
    val future = Connection.db.run(finalInsertQuery.transactionally)
    future.onComplete {
      case Failure(ex) => println(s"Query failed: $ex")
      case Success(_) => println(s"Query successful: Movie and Actor Inserted")
    }
  }


  def main(args: Array[String]): Unit = {
    //insertMovie(shawshank)
    //insertMovie(shallow)
    //readAllMovie()
    //readSpecificMovie()
    //insertMultipleActor(Seq(tomHanks, juliaRoberts))
    //readAllMoviesUsingPlainQuery()
    runMultipleQueriesInSingleTransaction()
    Thread.sleep(2000)
  }
}
