package fi.oph.koski.schema

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.JObject

/**
  * UI title for a property or object
  */
case class Title(title: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject) = obj
}
