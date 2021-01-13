package fi.oph.koski.http

import fi.oph.common.log.Loggable
import fi.oph.common.log.LogUtils.maskSensitiveInformation
import org.http4s.Request

/**
 *  Thrown when an external service returns an unexpected HTTP status code.
 */
case class HttpStatusException(status: Int, text: String, method: String, uri: String) extends LoggableException(status + ": " + text + " when requesting " + method + " " + uri)

object HttpStatusException {
  def apply(status: Int, text: String, request: Request): HttpStatusException = HttpStatusException(status, text, request.method.toString, request.uri.toString)
}

case class HttpConnectionException(text: String, method: String, uri: String) extends LoggableException(text + " when requesting " + method + " " + uri)

object HttpConnectionException {
  def apply(text: String, request: Request): HttpConnectionException = HttpConnectionException(text, request.method.toString, request.uri.toString)
}

abstract class LoggableException(msg: String) extends RuntimeException(msg) with Loggable {
  def logString = getMessage
  override def getMessage: String = maskSensitiveInformation(super.getMessage)
}
