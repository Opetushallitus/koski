package fi.oph.tor.http

import org.http4s.Request

case class HttpStatusException(status: Int, text: String, request: Request) extends RuntimeException(status + ": " + text + " when requesting " + request.method + " " + request.uri)