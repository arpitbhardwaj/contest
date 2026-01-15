package com.ab
package slickorm

import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport}

object CustomPostgresProfile extends ExPostgresProfile with PgArraySupport {
  override val api: API = new API {}

  trait API extends ExtPostgresAPI with ArrayImplicits {
    implicit val stringListTypeMapper: DriverJdbcType[List[String]] =
      new SimpleArrayJdbcType[String]("text").to(_.toList)
  }
}
