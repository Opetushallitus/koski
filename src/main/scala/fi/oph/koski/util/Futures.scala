package fi.oph.koski.util

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object Futures {
  def await[T](future: Future[T], atMost: Duration = 60 seconds): T = {
    Await.result(future, atMost)
  }
}
