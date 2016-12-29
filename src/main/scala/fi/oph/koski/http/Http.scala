package fi.oph.koski.http

import java.net.URLEncoder

import fi.oph.koski.http.Http.{Decode, runTask}
import fi.oph.koski.json.Json
import fi.oph.koski.log.{LoggerWithContext, Logging}
import fi.oph.koski.util.{Pools, Timer, Timing}
import io.prometheus.client.{Counter, Summary}
import org.http4s._
import org.http4s.client.blaze.BlazeClientConfig
import org.http4s.client.{Client, blaze}
import org.http4s.headers.`Content-Type`

import scala.concurrent.duration._
import scala.xml.Elem
import scalaz.concurrent.Task
import scalaz.stream.Process.Emit
import scalaz.{-\/, \/-}

object Http extends Logging {
  private val maxHttpConnections = Pools.jettyThreads + Pools.httpThreads
  def newClient = blaze.PooledHttp1Client(maxTotalConnections = maxHttpConnections, config = BlazeClientConfig.defaultConfig.copy(customExecutor = Some(Pools.httpPool)))

  // This guys allows you to make URIs from your Strings as in uri"http://google.com/s=${searchTerm}"
  // Takes care of URI encoding the components. You can prevent encoding a part by wrapping into an Uri using this selfsame method.
  implicit class UriInterpolator(val sc: StringContext) extends AnyVal {
    def uri(args: Any*): Uri = {
      val pairs: Seq[(String, Option[Any])] = sc.parts.zip(args.toList.map(Some(_)) ++ List(None))

      val stuff: Seq[String] = pairs.map { case (fixedPart, encodeablePartOption) =>
          fixedPart + (encodeablePartOption match {
            case None => ""
            case Some(encoded: Uri) => encoded.toString
            case Some(encodeable) => URLEncoder.encode(encodeable.toString, "UTF-8")
          })
      }
      uriFromString(stuff.mkString(""))
    }
  }

  // Warning: does not do any URI encoding
  def uriFromString(uri: String): Uri = {
    Uri.fromString(uri) match {
      case \/-(result) => result
      case -\/(failure) =>
        throw new IllegalArgumentException("Cannot create URI: " + uri + ": " + failure)
    }
  }

  def expectSuccess(status: Int, text: String, request: Request): Unit = (status, text) match {
    case (status, text) if status < 300 && status >= 200 =>
    case (status, text) => throw new HttpStatusException(status, text, request)
  }

  def parseJson[T](status: Int, text: String, request: Request)(implicit mf : scala.reflect.Manifest[T]): T = {
    (status, text) match {
      case (status, text) if (List(200, 201).contains(status)) => Json.read[T](text)
      case (status, text) => throw new HttpStatusException(status, text, request)
    }
  }

  def parseXml(status: Int, text: String, request: Request) = {
    (status, text) match {
      case (200, text) => scala.xml.XML.loadString(text)
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

  // Http task runner: runs at most 2 minutes. We must avoid using the timeout-less run method, that may block forever.
  def runTask[A](task: Task[A]): A = task.runFor(2 minutes)

  type Decode[ResultType] = (Int, String, Request) => ResultType

  object Encoders extends Logging {
    def xml: EntityEncoder[Elem] = EntityEncoder.stringEncoder(Charset.`UTF-8`).contramap[Elem](item => item.toString)
      .withContentType(`Content-Type`(MediaType.`text/xml`))
  }
}

case class Http(root: String, client: Client = Http.newClient) extends Logging {
  import Http.UriInterpolator
  private val rootUri = Http.uriFromString(root)

  def get[ResultType](uri: Uri)(decode: Decode[ResultType]): Task[ResultType] = {
    processRequest(Request(uri = uri))(decode)
  }

  def post[I <: AnyRef, O <: Any](path: Uri, entity: I)(encode: EntityEncoder[I])(decode: Decode[O]): Task[O] = {
    val request: Request = Request(uri = path, method = Method.POST)
    processRequest(request.withBody(entity)(encode))(decode)
  }

  def put[I <: AnyRef, O <: Any](path: Uri, entity: I)(encode: EntityEncoder[I])(decode: Decode[O]): Task[O] = {
    val request: Request = Request(uri = path, method = Method.PUT)
    processRequest(request.withBody(entity)(encode))(decode)
  }

  private def processRequest[ResultType](requestTask: Task[Request])(decode: Decode[ResultType]): Task[ResultType] = {
    requestTask.flatMap(request => processRequest(request)(decode))
  }

  private def processRequest[ResultType](request: Request)(decoder: (Int, String, Request) => ResultType): Task[ResultType] = {
    val requestWithFullPath: Request = request.copy(uri = addRoot(request.uri))
    val logger = HttpResponseLog(requestWithFullPath)
    client.fetch(addCommonHeaders(requestWithFullPath)) { response =>
      logger.log(response)
      response.as[String].map { text => // Might be able to optimize by not turning into String here
        decoder(response.status.code, text, request)
      }
    }.handleWith {
      case e: HttpStatusException =>
        logger.log(e)
        throw e
      case e: Exception =>
        logger.log(e)
        throw HttpConnectionException(e.getClass.getName + ": " + e.getMessage, requestWithFullPath)
    }
  }

  private def addRoot(uri: Uri) = {
    if (!uri.toString.startsWith("http")) {
      uri"${rootUri}${uri}"
    } else {
      uri
    }
  }

  private def addCommonHeaders(request: Request) = request.copy(headers = request.headers.put(
    Header("clientSubSystemCode", OpintopolkuSubSystemCode.koski)
  ))
}

protected object HttpResponseLog {
  val logger = LoggerWithContext(classOf[Http])
}

protected case class HttpResponseLog(request: Request) {
  private val started = System.currentTimeMillis
  def elapsedMillis = System.currentTimeMillis - started
  def log(response: Response) {
    log(response.status.code.toString, response.status.code < 400)
    HttpResponseMonitoring.record(request, response.status.code, elapsedMillis)
  }
  def log(e: HttpStatusException) {
    log(e.status.toString, e.status < 400)
    HttpResponseMonitoring.record(request, e.status, elapsedMillis)
  }
  def log(e: Exception) {
    log(e.getClass.getSimpleName, false)
    HttpResponseMonitoring.record(request, 500, elapsedMillis)
  }
  private def log(status: String, ok: Boolean) {
    val requestBody = if (ok) { None } else { request.body match {
      case Emit(seq) => seq.reduce(_ ++ _).decodeUtf8.right.toOption
      case _ => None
    }}.map("request body " + _).getOrElse("")

    HttpResponseLog.logger.debug(s"${request.method} ${request.uri} status ${status} took ${elapsedMillis} ms ${requestBody}")

  }
}

protected object HttpResponseMonitoring {
  private val statusCounter = Counter.build().name("fi_oph_koski_http_Http_status").help("Koski HTTP client response status").labelNames("service", "responseclass").register()
  private val durationDummary = Summary.build().name("fi_oph_koski_http_Http_duration").help("Koski HTTP client response duration").labelNames("service").register()
  private val HttpServicePattern = """https?:\/\/([a-z0-9\.-]+\/[a-z0-9\.-]+).*""".r

  def record(request: Request, status: Int, durationMillis: Long) {
    val responseClass = status / 100 * 100 // 100, 200, 300, 400, 500

    val service = request.uri.toString match {
      case HttpServicePattern(service) => service
      case _ => request.uri.toString
    }
    statusCounter.labels(service, responseClass.toString).inc
    durationDummary.labels(service).observe(durationMillis.toDouble / 1000)
  }
}