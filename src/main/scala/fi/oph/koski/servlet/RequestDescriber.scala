package fi.oph.koski.servlet

import javax.servlet.http.HttpServletRequest

import org.json4s.JsonAST.JString
import org.json4s._
import org.json4s.jackson.JsonMethods
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
          JsonMethods.compact(maskedJson)
        } catch {
          case e: Exception => body
        }
      case (body, Some(contentType)) if (contentType.contains("text/xml")) =>
        try {
          maskSensitiveInformationXml(body)
        } catch {
          case e: Exception => body
        }
      case (body, _) => body
    }
  }

  private def maskSensitiveInformation(parsedJson: JValue): JValue = {
    val maskedJson = parsedJson.mapField {
      case ("hetu", JString(_)) => ("hetu", JString("******-****"))
      case field: (String, JsonAST.JValue) => field
    }
    maskedJson
  }

  private def maskSensitiveInformationXml(xml: String): String = {
    xml.replaceAll("(hetu>)[-0-9A-Z]+(</)", "$1*$2")
  }

}
