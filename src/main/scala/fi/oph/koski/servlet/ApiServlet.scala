package fi.oph.koski.servlet

import fi.oph.koski.http.{ErrorCategory, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
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
          case None => haltWithStatus(KoskiErrorCategory.badRequest.format.json("Invalid JSON"))
        }
      case _ =>
        haltWithStatus(KoskiErrorCategory.unsupportedMediaType.jsonOnly())
    }
  }

  def renderOption[T <: AnyRef](errorCategory: ErrorCategory)(result: Option[T]) = {
    result match {
      case Some(x) => renderObject(x)
      case _ => haltWithStatus(errorCategory())
    }
  }

  def renderEither[T <: AnyRef](result: Either[HttpStatus, T]) = {
    result match {
      case Right(x) => renderObject(x)
      case Left(status) => haltWithStatus(status)
    }
  }

  def renderStatus(status: HttpStatus) = {
    response.setStatus(status.statusCode)
    renderObject(status.errors)
  }

  def renderObject(x: AnyRef): Unit = {
    contentType = "application/json;charset=utf-8"
    response.writer.print(Json.write(x))
  }
}
