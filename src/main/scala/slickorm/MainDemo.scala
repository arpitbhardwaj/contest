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

  def insertMovie(movie: Movie): Unit = {
    val insertQuery = SlickTables.movieTable += movie
    val futureId = Connection.db.run(insertQuery)
    futureId.onComplete {
      case Failure(ex) => println(s"Query failed: $ex")
      case Success(newMovieId) => println(s"Query successful: New Movie Id: $newMovieId")
    }
  }

  def main(args: Array[String]): Unit = {
    insertMovie(shawshank)
    Thread.sleep(2000)
  }
}
