package fi.oph.koski.json

import fi.oph.koski.log.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods

object JsonDiff extends Logging {
  def jsonDiff(oldValue: JValue, newValue: JValue): JArray = {
    JsonMethods.fromJsonNode(com.github.fge.jsonpatch.diff.JsonDiff.asJson(JsonMethods.asJsonNode(oldValue), JsonMethods.asJsonNode(newValue))).asInstanceOf[JArray]
  }
}
