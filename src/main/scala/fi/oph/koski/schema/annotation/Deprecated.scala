package fi.oph.koski.schema.annotation

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.JObject

// When field is deprecated it is hidden from the UI when it doesn't contain data
case class Deprecated(msg: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject): JObject = obj
}
