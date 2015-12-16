package fi.oph.tor.http

import fi.oph.tor.json.Json
import org.http4s._
import org.http4s.client.{Client, blaze}

import scalaz.concurrent.Task

object Http {
  def expectSuccess(status: Int, text: String, request: Request): Unit = (status, text) match {
    case (status, text) if status < 300 && status >= 200 =>
    case (status, text) => throw new HttpStatusException(status, text, request)
  }

  def parseJson[T](status: Int, text: String, request: Request)(implicit mf : scala.reflect.Manifest[T]): T = (status, text) match {
    case (200, text) => Json.read[T](text)
    case (status, text) => throw new HttpStatusException(status, text, request)
  }

  /** Parses as JSON, returns None on 404 result */
  def parseJsonOptional[T](status: Int, text: String, request: Request)(implicit mf : scala.reflect.Manifest[T]): Option[T] = (status, text) match {
    case (404, _) => None
    case (200, text) => Some(Json.read[T](text))
    case (status, text) => throw new HttpStatusException(status, text, request)
  }

  /** Parses as JSON, returns None on any error */
  def parseJsonIgnoreError[T](status: Int, text: String, request: Request)(implicit mf : scala.reflect.Manifest[T]): Option[T] = (status, text) match {
    case (200, text) => Some(Json.read[T](text))
    case (_, _) => None
  }

  def uriFromString(uri: String): Uri = {
    Uri.fromString(uri).toOption.get
  }
}

case class Http(client: Client = blaze.defaultClient) {
  def apply[ResultType](task: Task[Request], request: Request)(decode: (Int, String, Request) => ResultType): ResultType = {
    runHttp(client(task), request)(decode)
  }

  def apply[ResultType](request: Request)(decode: (Int, String, Request) => ResultType): ResultType = {
    runHttp(client(Task(request)), request)(decode)
  }

  def apply[ResultType](uri: Uri)(decode: (Int, String, Request) => ResultType): ResultType = {
    apply(Request(uri = uri))(decode)
  }

  def apply[ResultType](uri: String)(decode: (Int, String, Request) => ResultType): ResultType = {
    apply(Request(uri = Http.uriFromString(uri)))(decode)
  }

  def post[T <: AnyRef](path: Uri, entity: T)(implicit encode: EntityEncoder[T]): Unit = {
    apply(path, Method.POST, entity)
  }

  def put[T <: AnyRef](path: Uri, entity: T)(implicit encode: EntityEncoder[T]): Unit = {
    apply(path, Method.PUT, entity)
  }

  def apply[T <: AnyRef](path: Uri, method: Method, entity: T)(implicit encode: EntityEncoder[T]): Unit = {
    val request: Request = Request(uri = path, method = method)
    val task: Task[Request] = request.withBody(entity)

    apply(task, request) {
      case (status, text, uri) if (status >= 300) => throw new HttpStatusException(status, text, request)
      case _ =>
    }
  }

  private def runHttp[ResultType](task: Task[Response], request: Request)(block: (Int, String, Request) => ResultType): ResultType = {
    (for {
      response <- task
      text <- response.as[String]
    } yield {
        block(response.status.code, text, request)
      }).run
  }
}
