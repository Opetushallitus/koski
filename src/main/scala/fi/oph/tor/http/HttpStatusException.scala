package fi.oph.tor.http

import fi.oph.tor.log.Loggable
import org.http4s.Request

/**
 *  Thrown when an external service returns an unexpected HTTP status code.
 */
case class HttpStatusException(status: Int, text: String, method: String, uri: String) extends RuntimeException(status + ": " + text + " when requesting " + method + " " + uri) with Loggable {
  def this(status: Int, text: String, request: Request) = this(status, text, request.method.toString, request.uri.toString)

  override def toString = getMessage
}