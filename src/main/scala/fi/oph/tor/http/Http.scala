package fi.oph.tor.http

import fi.oph.tor.json.Json
import org.http4s.client.{Client, blaze}
import org.http4s.{Method, Request, Response, Uri}

import scalaz.concurrent.Task

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

  /** Parses as JSON, returns None on any error */
  def parseJsonIgnoreError[T](status: Int, text: String)(implicit mf : scala.reflect.Manifest[T]): Option[T] = (status, text) match {
    case (200, text) => Some(Json.read[T](text))
    case (_, _) => None
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

  def post[T <: AnyRef](path: Uri, entity: T)(implicit mf: Manifest[T]): Unit = {
    apply(path, Method.POST, entity)
  }

  def put[T <: AnyRef](path: Uri, entity: T)(implicit mf: Manifest[T]): Unit = {
    apply(path, Method.PUT, entity)
  }

  def apply[T <: AnyRef](path: Uri, method: Method, entity: T)(implicit mf: Manifest[T]): Unit = {
    import fi.oph.tor.json.Json._
    import fi.oph.tor.json.Json4sHttp4s._
    val task: Task[Request] = Request(uri = path, method = method).withBody(entity)(json4sEncoderOf[T])

    apply(task) {
      case (status, text) if (status >= 300) => throw new scala.RuntimeException(status + ": " + text)
      case _ =>
    }
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
