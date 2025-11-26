package fi.oph.koski.http

import cats.effect.IO
import fi.oph.koski.log.{LogUtils, Loggable}
import org.http4s.Request

/**
 *  Thrown when an external service returns an unexpected HTTP status code.
 */
case class HttpStatusException(status: Int, msg: String, method: String, uri: String)
  extends HttpException(s"$status: $msg", method, uri)

object HttpStatusException {
  def apply(status: Int, text: String, request: Request[IO]): HttpStatusException =
    HttpStatusException(status, text, request.method.toString, request.uri.toString)
}

case class HttpConnectionException(msg: String, method: String, uri: String)
  extends HttpException(msg, method, uri)

object HttpConnectionException {
  def apply(text: String, request: Request[IO]): HttpConnectionException =
    HttpConnectionException(text, request.method.toString, request.uri.toString)
}

case class DecodeException(innerE: Throwable, decoderId: String, request: Request[IO])
  extends HttpException(s"Decoder ${decoderId} failed: ${innerE.getMessage}", request)

abstract class HttpException(msg: String) extends RuntimeException(LogUtils.maskSensitiveInformation(msg)) with Loggable {

  def this(msg: String, method: String, uri: String) = this(s"${msg} when requesting ${method} ${uri}")

  def this(msg: String, request: Request[IO]) = this(msg, request.method.toString(), request.uri.toString())

  override def logString: String = getMessage
}
