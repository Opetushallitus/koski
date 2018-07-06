package fi.oph.koski.json

import com.github.fge.jsonpatch.diff.JsonDiff.asJson
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods.asJsonNode
import scala.reflect.runtime.universe.TypeTag

object JsonDiff {
  def jsonDiff(oldValue: JValue, newValue: JValue): JArray = {
    JsonMethods.fromJsonNode(asJson(asJsonNode(oldValue), asJsonNode(newValue))).asInstanceOf[JArray]
  }

  def objectDiff[A : TypeTag](a: A, b: A) = {
    jsonDiff(JsonSerializer.serializeWithRoot(a), JsonSerializer.serializeWithRoot(b))
  }
}

object JsonManipulation {
  def removeFields(o: JValue, fieldsToRemove: Set[String]) = {
    val JObject(foundFields) = o
    JObject(foundFields.filter(f => !fieldsToRemove.contains(f._1)))
  }
}
