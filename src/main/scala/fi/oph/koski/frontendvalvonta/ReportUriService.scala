package fi.oph.koski.frontendvalvonta

import fi.oph.koski.log.Logging
import org.json4s.{JBool, JObject, JValue}
import org.json4s.jackson.JsonMethods

// Huom: Älä muuta luokan nimeä: Muuten logien keruu myöhemmin putkessa hajoaa, koska logimerkinnät tunnistetaan
// log4j:ssä tällä hetkellä luokan nimen perusteella
class ReportUriService extends Logging {
  def report(request: JValue): Unit = {
    val valueWithTagField = request.merge(JObject("cspreporturi" -> JBool(true)))
    logger.info(JsonMethods.compact(JsonMethods.render(valueWithTagField)))
  }
}
