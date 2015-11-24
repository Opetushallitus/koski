package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.annotation.{Description, ReadOnly}
import fi.oph.tor.schema.generic._

object TorSchema {
  private val metadataTypes: List[MetadataSupport] = List(Description, KoodistoUri, KoodistoKoodiarvo, ReadOnly, OksaUri)
  lazy val schema: ClassSchema = SchemaFactory(metadataTypes).createSchema(classOf[TorOppija].getName)
  lazy val schemaJson = SchemaToJson.toJsonSchema(schema)(metadataTypes)
  lazy val schemaJsonString = Json.write(schemaJson)
  lazy val exampleJsonString = Json.write(TorOppijaExamples.perustutkintoNäyttönä)
}
