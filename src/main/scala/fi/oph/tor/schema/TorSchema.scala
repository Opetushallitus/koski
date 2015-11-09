package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.annotation.{Description, ReadOnly}
import fi.oph.tor.schema.generic.{ScalaJsonSchema, SchemaType}

import scala.reflect.runtime.universe

object TorSchema {
  val rootType = universe.typeOf[TorOppija]
  lazy val schema = new ScalaJsonSchema(Description, KoodistoUri, ReadOnly)
  lazy val schemaType: SchemaType = schema.createSchema(rootType)
  lazy val schemaJson = schema.toJsonSchema(schemaType)
  lazy val schemaJsonString = Json.write(schemaJson)
  lazy val exampleJsonString = Json.write(TorOppijaExamples.perustutkintoNäyttönä)
}
