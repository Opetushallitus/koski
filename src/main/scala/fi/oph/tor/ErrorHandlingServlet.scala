package fi.oph.tor

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.Json
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraServlet

trait ErrorHandlingServlet extends ScalatraServlet with Logging {
  def renderEither[T <: AnyRef](result: Either[HttpStatus, T]) = {
    contentType = "application/json;charset=utf-8"
    result match {
      case Right(x) => Json.write(x)
      case Left(HttpStatus(status, errors)) => halt(status, Json.write(errors))
    }
  }

  error {
    case InvalidRequestException(msg) =>
      halt(status = 400, msg)
    case e: Throwable =>
      // TODO: maskaa hetut
      val query: String = if (request.getQueryString == null) { "" } else { "?" + request.getQueryString }
      val requestDescription: String = request.getMethod + " " + request.getServletPath + query + " " + request.body
      logger.error("Error while processing request " + requestDescription, e)
      halt(status = 500, "Internal server error")
  }
}
