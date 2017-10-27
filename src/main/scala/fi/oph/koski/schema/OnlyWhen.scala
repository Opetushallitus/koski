package fi.oph.koski.schema

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST

case class OnlyWhen(path: String, dataPath: String, value: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JsonAST.JObject): JsonAST.JObject = appendToDescription(obj, s"Vain jos $path.$dataPath = $value")
}
