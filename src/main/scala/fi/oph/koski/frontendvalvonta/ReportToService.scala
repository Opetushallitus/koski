package fi.oph.koski.frontendvalvonta

import fi.oph.koski.log.Logging
import org.json4s.{JArray, JBool, JObject, JValue}
import org.json4s.jackson.JsonMethods

// Huom: Älä muuta luokan nimeä: Muuten logien keruu myöhemmin putkessa hajoaa, koska logimerkinnät tunnistetaan
// log4j:ssä tällä hetkellä luokan nimen perusteella
class ReportToService extends Logging {

  def report(request: JValue): Unit = {
    for {
      JArray(objList) <- request
      objValue <- objList
    } {
      val valueWithTagField = objValue.merge(JObject("frontendreportto" -> JBool(true)))
      logger.info(JsonMethods.compact(JsonMethods.render(valueWithTagField)))
    }
  }
}
