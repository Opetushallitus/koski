package fi.oph.tor.servlet

import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._
import fi.oph.tor.log.Logging
import org.json4s._
import org.scalatra.ScalatraServlet

trait KoskiBaseServlet extends ScalatraServlet with Logging {
  def getIntegerParam(name: String) = {
    params.getAs[Int](name) match {
      case Some(id) if id > 0 => id
      case _ =>
        // TODO: ensure handling
        throw new InvalidRequestException(TorErrorCategory.badRequest.format.number, "Invalid " + name + " : " + params(name))
    }
  }

  error {
    case InvalidRequestException(detail) =>
      renderStatus(detail)
    case e: Throwable =>
      renderInternalError(e)
  }

  def renderInternalError(e: Throwable) = {
    logger.error("Error while processing request " + describeRequest, e)
    renderStatus(TorErrorCategory.internalError())
  }

  def describeRequest: String = {
    val query: String = if (request.getQueryString == null) {""} else {"?" + request.getQueryString}
    val requestDescription: String = request.getMethod + " " + request.getServletPath + query + " " + maskRequestBody
    requestDescription
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

  def renderStatus(status: HttpStatus): Unit
}
