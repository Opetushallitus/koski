package fi.oph.koski.schema

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.JObject

/**
  * Indicates that a property can be used to represent the whole entity
  */
case class Representative() extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject) = obj
}
