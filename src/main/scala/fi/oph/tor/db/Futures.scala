package fi.oph.tor.db

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

trait Futures {
  def await[T](future: Future[T], atMost: Duration = 60 seconds)(implicit executor: ExecutionContext): T = {
    Await.result(future, atMost)
  }
}
