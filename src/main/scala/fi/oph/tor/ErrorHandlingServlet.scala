package fi.oph.tor

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.Json
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraServlet

trait ErrorHandlingServlet extends ScalatraServlet with Logging {
  def renderOption[T <: AnyRef](result: Option[T], pretty: Boolean = false) = result match {
    case Some(x) => Json.write(x, pretty)
    case _ => renderStatus(HttpStatus.notFound("Not found"))
  }

  def renderEither[T <: AnyRef](result: Either[HttpStatus, T], pretty: Boolean = false) = {
    contentType = "application/json;charset=utf-8"
    result match {
      case Right(x) => Json.write(x, pretty)
      case Left(status) => renderStatus(status)
    }
  }

  error {
    case InvalidRequestException(msg) =>
      renderStatus(HttpStatus.badRequest(msg))
    case e: Throwable =>
      // TODO: maskaa hetut
      val query: String = if (request.getQueryString == null) { "" } else { "?" + request.getQueryString }
      val requestDescription: String = request.getMethod + " " + request.getServletPath + query + " " + request.body
      logger.error("Error while processing request " + requestDescription, e)
      renderStatus(HttpStatus.internalError())
  }

  def renderStatus(status: HttpStatus) = {
    halt(status = status.statusCode, body = Json.write(status.errors))
  }
}
