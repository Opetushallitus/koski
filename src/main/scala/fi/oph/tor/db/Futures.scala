package fi.oph.tor.db

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

trait Futures {
  def await[T](future: Future[T])(implicit executor: ExecutionContext): T = {
    Await.result(future, 60 seconds)
  }
}
