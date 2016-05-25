package fi.oph.koski.db

import scala.concurrent.ExecutionContext

trait GlobalExecutionContext {
  implicit val executor = ExecutionContext.global // This is used by scala.concurrent package. We've overridden it's size in Spa.scala
}