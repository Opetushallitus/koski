package fi.oph.koski.frontendvalvonta

import fi.oph.koski.log.Logging
import org.json4s.{JArray, JBool, JObject, JValue}
import org.json4s.jackson.JsonMethods

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
