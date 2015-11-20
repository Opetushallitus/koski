package fi.oph.tor.schema.generic

import org.json4s.JsonAST
import org.json4s.JsonAST.{JNothing, JString, JObject}

trait Metadata

trait MetadataSupport {
  val extractMetadata: PartialFunction[(String, List[String]), List[Metadata]]
  def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata): JObject
  def appendToDescription(obj: JObject, koodisto: String): JsonAST.JObject = {
    val description = obj.\("description") match {
      case JString(s) => s
      case JNothing => ""
    }
    obj.merge(JObject("description" -> JString(description + koodisto)))
  }
}
