package fi.oph.koski.frontendvalvonta

import fi.oph.koski.log.{LogUtils, Logging, MaskedSlf4jRequestLogWriter}
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
    val maskedBody = MaskedSlf4jRequestLogWriter.maskSensitiveInformationFrontendUris(
      LogUtils.maskSensitiveInformation(request.body)
    )
    reportUriService.report(JsonMethods.parse(maskedBody))
  }

  post("/report-to") {
    checkBodySize
    checkContentTypeAndEncoding(List("application/json", "application/reports+json"))
    val maskedBody = MaskedSlf4jRequestLogWriter.maskSensitiveInformationFrontendUris(
      LogUtils.maskSensitiveInformation(request.body)
    )
    reportToService.report(JsonMethods.parse(maskedBody))
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
