package fi.oph.koski.frontendvalvonta

import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{CacheControlSupport, NoCache, TimedServlet}
import org.json4s.jackson.JsonMethods
import org.scalatra.{ContentEncodingSupport, ScalatraServlet}

class FrontendValvontaRaportointiServlet extends ScalatraServlet with Logging with TimedServlet with ContentEncodingSupport with CacheControlSupport with NoCache {

  val MAX_BODY_SIZE = 32000

  val reportUriService = new ReportUriService
  val reportToService = new ReportToService

  post("/report-uri") {
    checkBodySize
    checkContentTypeAndEncoding(List("application/json", "application/csp-report"))
    reportUriService.report(JsonMethods.parse(request.body))
  }

  post("/report-to") {
    checkBodySize
    checkContentTypeAndEncoding(List("application/json", "application/reports+json"))
    reportToService.report(JsonMethods.parse(request.body))
  }

  private def checkBodySize: Unit = {
    if (request.body.size > MAX_BODY_SIZE) {
      logger.warn(s"Yli ${MAX_BODY_SIZE} merkin sisältö")
      halt(403)
    }
  }

  private def checkContentTypeAndEncoding(validContentTypes: List[String]): Unit = {
    val contentType = request.contentType.map(_.split(";")(0).toLowerCase)
    val encoding = request.characterEncoding.map(_.toLowerCase)

    if (encoding != Some("utf-8") || !contentType.exists(validContentTypes.contains)) {
      logger.warn(s"Epäkelpo json-sisältö ${contentType} ${encoding}")
      halt(400)
    }
  }
}
