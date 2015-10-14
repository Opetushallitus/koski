package fi.oph.tor.http

import fi.oph.tor.json.Json
import org.http4s.{Uri, Response, Request}
import org.http4s.client.Client
import scalaz.concurrent.Task
import org.http4s.client.blaze

object Http {
  def parseJson[T](status: Int, text: String)(implicit mf : scala.reflect.Manifest[T]): T = (status, text) match {
    case (200, text) => Json.read[T](text)
    case (status, text) => throw new RuntimeException(status + ": " + text)
  }

  /** Parses as JSON, returns None on 404 result */
  def parseJsonOptional[T](status: Int, text: String)(implicit mf : scala.reflect.Manifest[T]): Option[T] = (status, text) match {
    case (404, _) => None
    case (200, text) => Some(Json.read[T](text))
    case (status, text) => throw new RuntimeException(status + ": " + text)
  }

  def uriFromString(url: String): Uri = {
    Uri.fromString(url).toOption.get
  }
}

case class Http(client: Client = blaze.defaultClient) {
  def apply[ResultType](task: Task[Request])(decode: (Int, String) => ResultType): ResultType = {
    runHttp(client(task))(decode)
  }

  def apply[ResultType](request: Request)(decode: (Int, String) => ResultType): ResultType = {
    apply(Task(request))(decode)
  }

  def apply[ResultType](uri: Uri)(decode: (Int, String) => ResultType): ResultType = {
    apply(Task(Request(uri = uri)))(decode)
  }

  def apply[ResultType](uri: String)(decode: (Int, String) => ResultType): ResultType = {
    apply(Task(Request(uri = Http.uriFromString(uri))))(decode)
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
