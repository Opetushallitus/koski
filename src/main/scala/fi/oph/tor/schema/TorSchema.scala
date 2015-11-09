package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.annotation.{Description, ReadOnly}
import fi.oph.tor.schema.generic.{ScalaJsonSchema, SchemaType}

import scala.reflect.runtime.universe

object TorSchema {
  val rootType = universe.typeOf[TorOppija]
  val schema = new ScalaJsonSchema(Description, KoodistoUri, ReadOnly)
  val schemaType: SchemaType = schema.createSchema(rootType)
  val schemaJson = schema.toJsonSchema(schemaType)
  val schemaJsonString = Json.write(schemaJson)
  val exampleJsonString = Json.write(TorOppijaExamples.full)
}
