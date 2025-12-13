package com.ab
package slickorm

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * @author Arpit Bhardwaj
 *
 */

object PrivateExecutionContext {
  val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(executor)
}

object MainDemo {
  import slick.jdbc.PostgresProfile.api._
  import PrivateExecutionContext._

  val shawshank = Movie(1L, "Shawshank Redemption", LocalDate.of(1994, 4, 2), 162)
  val shallow = Movie(1L, "Shallow", LocalDate.of(2001, 4, 2), 132)

  private def insertMovie(movie: Movie): Unit = {
    val insertQuery = SlickTables.movieTable += movie
    val futureId = Connection.db.run(insertQuery)
    futureId.onComplete {
      case Failure(ex) => println(s"Query failed: $ex")
      case Success(newMovieId) => println(s"Query successful: New Movie Id: $newMovieId")
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

  def main(args: Array[String]): Unit = {
    //insertMovie(shawshank)
    //insertMovie(shallow)
    readAllMovie()
    Thread.sleep(2000)
  }
}
