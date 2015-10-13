package fi.oph.tor.http

import fi.oph.tor.json.Json
import org.http4s.{Response, Request}
import org.http4s.client.Client
import scalaz.concurrent.Task

object Http {
  def parseJson[T](status: Int, text: String)(implicit mf : scala.reflect.Manifest[T]): T = (status, text) match {
    case (200, text) => Json.read[T](text)
    case (status, text) => throw new RuntimeException(status + ": " + text)
  }
}

case class Http(client: Client) {
  def apply[ResultType](task: Task[Request])(decode: (Int, String) => ResultType): ResultType = {
    runHttp(client(task))(decode)
  }

  private def runHttp[ResultType](task: Task[Response])(block: (Int, String) => ResultType): ResultType = {
    (for {
      response <- task
      text <- response.as[String]
    } yield {
        block(response.status.code, text)
      }).run
  }
}
