package fi.oph.tor.db

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait Futures {
  def await[T](future: Future[T], atMost: Duration = 60 seconds): T = {
    Await.result(future, atMost)
  }
}
