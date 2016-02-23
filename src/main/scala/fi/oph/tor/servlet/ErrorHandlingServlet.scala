package fi.oph.tor.servlet

import fi.oph.tor.http.{ErrorCategory, TorErrorCategory, HttpStatus}
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json.maskSensitiveInformation
import fi.oph.tor.log.Logging
import fi.oph.tor.util.Timing
import org.json4s._
import org.scalatra.ScalatraServlet

trait ErrorHandlingServlet extends ScalatraServlet with Logging with Timing {
  def withJsonBody(block: JValue => Any) = {
    if (request.getContentType != "application/json") {
      renderStatus(TorErrorCategory.unsupportedMediaType.jsonOnly())
    } else {
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

  error {
    case InvalidRequestException(detail) =>
      renderStatus(detail)
    case e: Throwable =>
      renderInternalError(e)
  }

  def renderInternalError(e: Throwable): Nothing = {
    logger.error("Error while processing request " + describeRequest, e)
    renderStatus(TorErrorCategory.internalError())
  }

  def describeRequest: String = {
    val query: String = if (request.getQueryString == null) {""} else {"?" + request.getQueryString}
    val requestDescription: String = request.getMethod + " " + request.getServletPath + query + " " + maskRequestBody
    requestDescription
  }

  def renderStatus(status: HttpStatus) = {
    halt(status = status.statusCode, body = Json.write(status.errors))
  }

  def getIntegerParam(name: String): Int = {
    params.getAs[Int](name) match {
      case Some(id) if id > 0 => id
      case _ => throw new InvalidRequestException(TorErrorCategory.badRequest.format.number, "Invalid " + name + " : " + params(name))
    }
  }

  private def maskRequestBody = {
    (request.body, request.contentType) match {
      case ("", _) => ""
      case (body, Some(contentType)) if (contentType.contains("application/json")) =>
        try {
          val parsedJson: JValue = org.json4s.jackson.JsonMethods.parse(request.body)
          val maskedJson: JValue = maskSensitiveInformation(parsedJson)
          Json.write(maskedJson)
        } catch {
          case e: Exception => body
        }
      case (body, _) => body
    }
  }
}
