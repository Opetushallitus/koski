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

  def renderStatus(status: HttpStatus) = {
    response.setStatus(status.statusCode)
    renderObject(status.errors)
  }

  def renderObject(x: AnyRef): Unit = {
    contentType = "application/json;charset=utf-8"
    response.writer.print(Json.write(x))
  }
}
