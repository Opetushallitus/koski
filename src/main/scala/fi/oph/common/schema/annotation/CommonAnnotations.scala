package fi.oph.common.schema.annotation

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.JObject

trait RepresentationalMetadata extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject) = obj // Does not affect JSON schema
}

/* This property can be used to represent the whole entity */
case class Representative() extends RepresentationalMetadata
