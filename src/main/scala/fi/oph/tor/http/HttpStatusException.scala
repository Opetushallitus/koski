package fi.oph.tor.http

import fi.oph.tor.log.Loggable
import org.http4s.Request

case class HttpStatusException(status: Int, text: String, request: Request) extends RuntimeException(status + ": " + text + " when requesting " + request.method + " " + request.uri) with Loggable {
  override def toString = getMessage
}