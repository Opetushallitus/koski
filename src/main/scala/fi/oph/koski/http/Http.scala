package fi.oph.koski.http

import cats.effect.IO
import cats.effect.unsafe.{IORuntime, IORuntimeConfig}
import fi.oph.koski.executors.Pools
import fi.oph.koski.http.Http.{Decode, ParameterizedUriWrapper, UriInterpolator}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.LogUtils.maskSensitiveInformation
import fi.oph.koski.log.{LoggerWithContext, Logging}
import io.prometheus.client.{Counter, Summary}
import org.http4s._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.headers.`Content-Type`
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.parse
import org.typelevel.ci.CIString

import java.net.URLEncoder
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.reflect.runtime.universe.TypeTag
import scala.xml.Elem

object Http extends Logging {
  private implicit val ioPool: IORuntime = IORuntime(
    compute = Pools.globalExecutor,
    blocking = Pools.httpExecutionContext,
    scheduler = IORuntime.createDefaultScheduler()._1,
    shutdown = () => (), // Will live forever
    config = IORuntimeConfig()
  )

  private val maxHttpConnections = Pools.httpThreads

  def newClient(name: String): Client[IO] = {
    logger.info(s"Creating new pooled http client with $maxHttpConnections max total connections for $name")
    BlazeClientBuilder[IO](ExecutionContext.fromExecutor(Pools.httpPool))
      .withMaxTotalConnections(maxHttpConnections)
      .withMaxWaitQueueLimit(1024)
      .withConnectTimeout(20.seconds)
      .withResponseHeaderTimeout(30.seconds)
      .withRequestTimeout(1.minutes)
      .withIdleTimeout(2.minutes)
      .allocated
      .map(_._1)
      .unsafeRunSync()
  }

  // This guys allows you to make URIs from your Strings as in uri"http://google.com/s=${searchTerm}"
  // Takes care of URI encoding the components. You can prevent encoding a part by wrapping into an Uri using this selfsame method.
  implicit class UriInterpolator(val sc: StringContext) extends AnyVal {
    def uri(args: Any*): ParameterizedUriWrapper = {
      val pairs: Seq[(String, Option[Any])] = sc.parts.zip(args.toList.map(Some(_)) ++ List(None))

      val parameterized: String = pairs.map { case (fixedPart, encodeablePartOption) =>
          fixedPart + (encodeablePartOption match {
            case None => ""
            case Some(encoded: ParameterizedUriWrapper) => encoded.uri.toString
            case Some(encoded: Uri) => encoded.toString
            case Some(encodeable) => URLEncoder.encode(encodeable.toString, "UTF-8")
          })
      }.mkString("")
      val template: String = pairs.map { case (fixedPart, encodeablePartOption) =>
        fixedPart + (encodeablePartOption match {
          case None => ""
          case Some(_) => "_"
        })
      }.mkString("").replaceAll("__", "_")

      ParameterizedUriWrapper(uriFromString(parameterized), template)
    }
  }

  // Warning: does not do any URI encoding
  // For when you don't want encoding but Http.scala wants a ParameterizedUriWrapper
  implicit class StringToUriConverter(s: String) {
    def toUri: ParameterizedUriWrapper = ParameterizedUriWrapper(uriFromString(s), s)
  }

  case class ParameterizedUriWrapper(uri: Uri, template: String)

  // Warning: does not do any URI encoding
  private def uriFromString(uri: String): Uri = {
    Uri.fromString(uri) match {
      case Right(result) => result
      case Left(failure) =>
        throw new IllegalArgumentException("Cannot create URI: " + uri + ": " + failure)
    }
  }

  def expectSuccess(status: Int, text: String, request: Request[IO]): Unit = (status, text) match {
    case (status, text) if status < 300 && status >= 200 =>
    case (status, text) => throw HttpStatusException(status, text, request)
  }

  def parseJson[T : TypeTag](status: Int, text: String, request: Request[IO]): T = {
    (status, text) match {
      case (status, text) if (List(200, 201).contains(status)) => JsonSerializer.extract[T](parse(text), ignoreExtras = true)
      case (status, text) => throw HttpStatusException(status, text, request)
    }
  }

  def parseJsonWithDeserialize[T : TypeTag](deserialize: JValue => T)(status: Int, text: String, request: Request[IO]): T = {
    (status, text) match {
      case (status, text) if (List(200, 201).contains(status)) => deserialize(parse(text))
      case (status, text) => throw HttpStatusException(status, text, request)
    }
  }

  def parseXml(status: Int, text: String, request: Request[IO]): Elem = {
    (status, text) match {
      case (200, text) => scala.xml.XML.loadString(text)
      case (status, text) => throw HttpStatusException(status, text, request)
    }
  }

  /** Parses as JSON, returns None on 404 result */
  def parseJsonOptional[T](status: Int, text: String, request: Request[IO])(implicit mf : Manifest[T]): Option[T] = (status, text) match {
    case (404, _) => None
    case (200, text) => Some(JsonSerializer.extract[T](parse(text), ignoreExtras = true))
    case (status, text) => throw HttpStatusException(status, text, request)
  }

  def toString(status: Int, text: String, request: Request[IO]): String = (status, text) match {
    case (200, text) => text
    case (status, text) => throw HttpStatusException(status, text, request)
  }

  def statusCode(status: Int, text: String, request: Request[IO]): Int = (status, text) match {
    case (code, _) => code
  }

  val unitDecoder: Decode[Unit] =  {
    case (status, text, request) if (status >= 300) => throw HttpStatusException(status, text, request)
    case _ =>
  }

  def runIO[A](io: IO[A], timeout: FiniteDuration = 2.minutes): A = io.timeout(timeout).unsafeRunSync()

  type Decode[ResultType] = (Int, String, Request[IO]) => ResultType

  object Encoders {
    def xml: EntityEncoder[IO, Elem] = EntityEncoder.stringEncoder[IO](Charset.`UTF-8`).contramap[Elem](item => item.toString)
      .withContentType(`Content-Type`(MediaType.text.`xml`))
    def formData: EntityEncoder[IO, String] = EntityEncoder.stringEncoder[IO].withContentType(`Content-Type`(MediaType.application.`x-www-form-urlencoded`))
  }

  def apply(root: String, name: String): Http = Http(root, newClient(name))

}

case class Http(root: String, client: Client[IO]) extends Logging {
  private val rootUri = Http.uriFromString(root)

  private val commonHeaders = Headers(Header.Raw(CIString("Caller-Id"), OpintopolkuCallerId.koski))

  def get[ResultType](uri: ParameterizedUriWrapper, headers: Headers = Headers.empty)(decode: Decode[ResultType]): IO[ResultType] = {
    processRequest(Request(uri = uri.uri, headers = headers), uri.template)(decode)
  }

  def head[ResultType](uri: ParameterizedUriWrapper, headers: Headers = Headers.empty)(decode: Decode[ResultType]): IO[ResultType] = {
    processRequest(Request(uri = uri.uri, headers = headers, method = Method.HEAD), uri.template)(decode)
  }

  def post[I <: AnyRef, O <: Any](path: ParameterizedUriWrapper, entity: I)(encode: EntityEncoder[IO, I])(decode: Decode[O]): IO[O] = {
    val request: Request[IO] = Request(uri = path.uri, method = Method.POST)
    processRequest(request.withEntity(entity)(encode), uriTemplate = path.template)(decode)
  }

  def post[ResultType](uri: ParameterizedUriWrapper, headers: Headers)(decode: Decode[ResultType]): IO[ResultType] = {
    processRequest(Request(uri = uri.uri, method = Method.POST, headers = headers), uri.template)(decode)
  }

  def put[I <: AnyRef, O <: Any](path: ParameterizedUriWrapper, entity: I)(encode: EntityEncoder[IO, I])(decode: Decode[O]): IO[O] = {
    val request: Request[IO] = Request(uri = path.uri, method = Method.PUT)
    processRequest(request.withEntity(entity)(encode), path.template)(decode)
  }

  private def processRequest[ResultType]
    (request: Request[IO], uriTemplate: String)
    (decoder: (Int, String, Request[IO]) => ResultType)
  : IO[ResultType] = {
    val fullRequest = request
      .withUri(addRoot(request.uri))
      .withHeaders(request.headers ++ commonHeaders)

    val httpLogger = HttpResponseLog(fullRequest, root + uriTemplate)

    client
      .run(fullRequest)
      .use { response =>
        httpLogger.log(response.status)
        // TODO: Toteuta decoderit EntityDecoder-tyyppisinä
        response.as[String].map(body => decoder(response.status.code, body, request))
      }
      .handleError {
        case e: HttpStatusException =>
          httpLogger.log(e)
          throw e
        case e: Exception =>
          httpLogger.log(e)
          throw HttpConnectionException(e.getClass.getName + ": " + e.getMessage, fullRequest)
      }
  }

  private def addRoot(uri: Uri) = {
    if (!uri.toString.startsWith("http")) {
      uri"${rootUri}${uri}".uri
    } else {
      uri
    }
  }
}

protected object HttpResponseLog {
  val logger: LoggerWithContext = LoggerWithContext(classOf[Http])
}

protected case class HttpResponseLog(request: Request[IO], uriTemplate: String) {
  private val started = System.currentTimeMillis

  private def elapsedMillis = System.currentTimeMillis - started

  def log(responseStatus: Status) {
    log(responseStatus.code.toString)
    HttpResponseMonitoring.record(request, uriTemplate, responseStatus.code, elapsedMillis)
  }

  def log(e: HttpStatusException) {
    log(e.status.toString)
    HttpResponseMonitoring.record(request, uriTemplate, e.status, elapsedMillis)
  }

  def log(e: Exception) {
    log(e.getClass.getSimpleName)
    HttpResponseMonitoring.record(request, uriTemplate, 500, elapsedMillis)
  }

  private def log(status: String) {
    HttpResponseLog.logger.debug(
      maskSensitiveInformation(s"${request.method} ${request.uri} status ${status} took ${elapsedMillis} ms")
    )
  }
}

protected object HttpResponseMonitoring {
  private val statusCounter = Counter.build().name("fi_oph_koski_http_Http_status").help("Koski HTTP client response status").labelNames("service", "endpoint", "responseclass").register()
  private val durationDummary = Summary.build().name("fi_oph_koski_http_Http_duration").help("Koski HTTP client response duration").labelNames("service", "endpoint").register()

  private val HttpServicePattern = """https?://([a-z0-9:.-]+/[a-z0-9.-]+).*""".r

  def record(request: Request[IO], uriTemplate: String, status: Int, durationMillis: Long) {
    val responseClass = status / 100 * 100 // 100, 200, 300, 400, 500

    val service = request.uri.toString match {
      case HttpServicePattern(service) => service
      case _ => request.uri.toString
    }
    val endpoint = request.method + " " + uriTemplate

    statusCounter.labels(service, endpoint, responseClass.toString).inc
    durationDummary.labels(service, endpoint).observe(durationMillis.toDouble / 1000)
  }
}
