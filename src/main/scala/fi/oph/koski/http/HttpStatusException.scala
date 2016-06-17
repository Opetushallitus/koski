package fi.oph.koski.http

import fi.oph.koski.log.Loggable
import org.http4s.Request

/**
 *  Thrown when an external service returns an unexpected HTTP status code.
 */
case class HttpStatusException(status: Int, text: String, request: Request) extends RuntimeException(status + ": " + text + " when requesting " + request.method.toString + " " + request.uri.toString) with Loggable {
  def logString = getMessage
}