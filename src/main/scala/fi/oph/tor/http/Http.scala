package fi.oph.tor.http

import fi.oph.tor.http.Http.Decode
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

  val unitDecoder: Decode[Unit] =  {
    case (status, text, request) if (status >= 300) => throw new HttpStatusException(status, text, request)
    case _ =>
  }

  def uriFromString(uri: String): Uri = {
    Uri.fromString(uri).toOption.get
  }

  type Decode[ResultType] = (Int, String, Request) => ResultType
}

case class Http(root: String, client: Client = blaze.defaultClient) {
  def uriFromString(relativePath: String) = Http.uriFromString(root + relativePath)

  def apply[ResultType](task: Task[Request], request: Request)(decode: Decode[ResultType]): Task[ResultType] = {
    runHttp(client(task), request)(decode)
  }

  def apply[ResultType](request: Request)(decode: Decode[ResultType]): Task[ResultType] = {
    runHttp(client(Task(request)), request)(decode)
  }

  def apply[ResultType](uri: Uri)(decode: Decode[ResultType]): Task[ResultType] = {
    apply(Request(uri = uri))(decode)
  }

  def apply[ResultType](uri: String)(decode: Decode[ResultType]): Task[ResultType] = {
    apply(Request(uri = Http.uriFromString(root + uri)))(decode)
  }

  def post[I <: AnyRef, O <: Any](path: String, entity: I)(implicit encode: EntityEncoder[I], decode: Decode[O]): O = {
    val request: Request = Request(uri = uriFromString(path), method = Method.POST)
    val task: Task[Request] = request.withBody(entity)

    apply(task, request)(decode).run
  }

  def put[T <: AnyRef](path: String, entity: T)(implicit encode: EntityEncoder[T]): Unit = {
    send(uriFromString(path), Method.PUT, entity)
  }

  def send[T <: AnyRef](path: Uri, method: Method, entity: T)(implicit encode: EntityEncoder[T]): Unit = {
    val request: Request = Request(uri = path, method = method)
    val task: Task[Request] = request.withBody(entity)

    apply(task, request) (Http.unitDecoder).run
  }

  private def runHttp[ResultType](task: Task[Response], request: Request)(decoder: (Int, String, Request) => ResultType): Task[ResultType] = {
    (for {
      response <- task
      text <- response.as[String]
    } yield {
        decoder(response.status.code, text, request)
      })
  }
}
