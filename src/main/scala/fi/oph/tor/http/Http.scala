package fi.oph.tor.http

import fi.oph.tor.http.Http.{runTask, Decode}
import fi.oph.tor.json.Json
import org.http4s._
import org.http4s.client.{Client, blaze}
import fi.oph.tor.log.Logging
import concurrent.duration._
import scalaz.concurrent.Task

object Http extends Logging {
  def newClient = blaze.PooledHttp1Client(maxTotalConnections = 40)

  def expectSuccess(status: Int, text: String, request: Request): Unit = (status, text) match {
    case (status, text) if status < 300 && status >= 200 =>
    case (status, text) => throw new HttpStatusException(status, text, request)
  }

  def parseJson[T](status: Int, text: String, request: Request)(implicit mf : scala.reflect.Manifest[T]): T = {
    (status, text) match {
      case (200, text) => Json.read[T](text)
      case (status, text) => throw new HttpStatusException(status, text, request)
    }
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

  def toString(status: Int, text: String, request: Request) = (status, text) match {
    case (200, text) => text
    case (status, text) => throw new HttpStatusException(status, text, request)
  }

  def statusCode(status: Int, text: String, request: Request) = (status, text) match {
    case (code, _) => code
  }

  val unitDecoder: Decode[Unit] =  {
    case (status, text, request) if (status >= 300) => throw new HttpStatusException(status, text, request)
    case _ =>
  }

  def uriFromString(uri: String): Uri = {
    Uri.fromString(uri).toOption.get
  }

  // Http task runner: runs at most 2 minutes. We must avoid using the timeout-less run method, that may block forever.
  def runTask[A](task: Task[A]): A = {
    task.runFor(2 minutes)
  }

  type Decode[ResultType] = (Int, String, Request) => ResultType
}

case class Http(root: String, client: Client = Http.newClient) extends Logging {
  def uriFromString(relativePath: String) = Http.uriFromString(root + relativePath)

  def apply[ResultType](request: Request)(decode: Decode[ResultType]): Task[ResultType] = {
    processRequest(request)(decode)
  }

  def apply[ResultType](requestTask: Task[Request])(decode: Decode[ResultType]): Task[ResultType] = {
    requestTask.flatMap(request => processRequest(request)(decode))
  }

  def apply[ResultType](uri: Uri)(decode: Decode[ResultType]): Task[ResultType] = {
    apply(Request(uri = uri))(decode)
  }

  def apply[ResultType](uri: String = "")(decode: Decode[ResultType]): Task[ResultType] = {
    apply(Request(uri = Http.uriFromString(root + uri)))(decode)
  }

  def post[I <: AnyRef, O <: Any](path: String, entity: I)(implicit encode: EntityEncoder[I], decode: Decode[O]): O = {
    send(uriFromString(path), Method.POST, entity)
  }

  def put[I <: AnyRef, O <: Any](path: String, entity: I)(implicit encode: EntityEncoder[I], decode: Decode[O]): O = {
    send(uriFromString(path), Method.PUT, entity)
  }

  def send[I <: AnyRef, O <: Any](path: Uri, method: Method, entity: I)(implicit encode: EntityEncoder[I], decode: Decode[O]): O = {
    val request: Request = Request(uri = path, method = method)
    val requestTask: Task[Request] = request.withBody(entity)
    runTask(apply(requestTask)(decode))
  }

  private def processRequest[ResultType](request: Request)(decoder: (Int, String, Request) => ResultType): Task[ResultType] = {
    client.fetch(request) { response =>
      response.as[String].map { text => // TODO: don't convert to string here
        decoder(response.status.code, text, request)
      }
    }
  }
}
