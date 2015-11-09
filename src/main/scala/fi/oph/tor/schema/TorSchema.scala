package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.fi.oph.tor.schema.generic.ReadOnly
import fi.oph.tor.schema.generic.{SchemaType, DescriptionAnnotation, ScalaJsonSchema}
import scala.reflect.runtime.universe

object TorSchema {
  val rootType = universe.typeOf[TorOppija]
  val schema = new ScalaJsonSchema(DescriptionAnnotation, KoodistoAnnotation, ReadOnly)
  val schemaType: SchemaType = schema.createSchema(rootType)
  val schemaJson = schema.toJsonSchema(schemaType)
  val schemaJsonString = Json.write(schemaJson)
  val exampleJsonString = Json.write(TorOppijaExamples.full)
}
