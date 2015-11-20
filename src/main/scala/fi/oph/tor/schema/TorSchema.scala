package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.annotation.{Description, ReadOnly}
import fi.oph.tor.schema.generic.{ScalaJsonSchema, SchemaToJson, SchemaType}

object TorSchema {
  implicit lazy val schema = new ScalaJsonSchema(Description, KoodistoUri, ReadOnly, OksaUri)
  lazy val schemaType: SchemaType = schema.createSchemaType(classOf[TorOppija].getName)
  lazy val schemaJson = SchemaToJson.toJsonSchema(schemaType)
  lazy val schemaJsonString = Json.write(schemaJson)
  lazy val exampleJsonString = Json.write(TorOppijaExamples.perustutkintoNäyttönä)
}
