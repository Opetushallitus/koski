package fi.oph.koski.servlet

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import org.json4s._
import org.scalatra.servlet.RichRequest

object RequestDescriber {
  def logSafeDescription(request: HttpServletRequest): String = {
    val query: String = if (request.getQueryString == null) {""} else {"?" + request.getQueryString}
    val requestDescription: String = request.getMethod + " " + request.getServletPath + query + " " + maskRequestBody(RichRequest(request))
    requestDescription
  }

  private def maskRequestBody(request: RichRequest) = {
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
