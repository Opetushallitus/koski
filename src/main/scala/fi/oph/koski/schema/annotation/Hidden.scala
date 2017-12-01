package fi.oph.koski.schema.annotation

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.JObject

/**
  * Flag for hiding properties from the UI
  */
case class Hidden() extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject) = obj
}
