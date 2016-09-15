package fi.oph.koski.http

import fi.oph.koski.log.Loggable
import org.http4s.Request

/**
 *  Thrown when an external service returns an unexpected HTTP status code.
 */
case class HttpStatusException(status: Int, text: String, request: Request) extends LoggableException(status + ": " + text + " when requesting " + request.method.toString + " " + request.uri.toString)

case class HttpConnectionException(text: String, request: Request) extends LoggableException(text + " when requesting " + request.method.toString + " " + request.uri.toString)

abstract class LoggableException(msg: String) extends RuntimeException(msg) with Loggable {
  def logString = getMessage
}