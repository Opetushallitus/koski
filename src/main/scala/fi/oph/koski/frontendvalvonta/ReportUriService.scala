package fi.oph.koski.frontendvalvonta

import fi.oph.koski.log.Logging
import org.json4s.{JBool, JObject, JValue}
import org.json4s.jackson.JsonMethods

class ReportUriService extends Logging {
  def report(request: JValue): Unit = {
    val valueWithTagField = request.merge(JObject("cspreporturi" -> JBool(true)))
    logger.info(JsonMethods.compact(JsonMethods.render(valueWithTagField)))
  }
}
