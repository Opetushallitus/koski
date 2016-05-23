package fi.oph.tor.servlet

import fi.oph.tor.http.{ErrorCategory, HttpStatus, TorErrorCategory}
import fi.oph.tor.json.Json
import fi.oph.tor.log.Logging
import fi.oph.tor.util.Timing
import org.json4s._

trait ApiServlet extends KoskiBaseServlet with Logging with Timing {
  def withJsonBody(block: JValue => Any) = {
    (request.contentType.map(_.split(";")(0).toLowerCase), request.characterEncoding.map(_.toLowerCase)) match {
      case (Some("application/json"), Some("utf-8")) =>
        val json = timed("json parsing") {
          try {
            Some(org.json4s.jackson.JsonMethods.parse(request.body))
          } catch {
            case e: Exception => None
          }
        }
        json match {
          case Some(json) => block(json)
          case None => renderStatus(TorErrorCategory.badRequest.format.json("Invalid JSON"))
        }
      case _ =>
        renderStatus(TorErrorCategory.unsupportedMediaType.jsonOnly())
    }
  }

  def renderOption[T <: AnyRef](errorCategory: ErrorCategory)(result: Option[T], pretty: Boolean = false) = {
    contentType = "application/json;charset=utf-8"
    result match {
      case Some(x) => Json.write(x, pretty)
      case _ => renderStatus(errorCategory())
    }
  }

  def renderEither[T <: AnyRef](result: Either[HttpStatus, T], pretty: Boolean = false) = {
    contentType = "application/json;charset=utf-8"
    result match {
      case Right(x) => Json.write(x, pretty)
      case Left(status) => renderStatus(status)
    }
  }

  def renderStatus(status: HttpStatus) = {
    halt(status = status.statusCode, body = Json.write(status.errors))
  }
}
