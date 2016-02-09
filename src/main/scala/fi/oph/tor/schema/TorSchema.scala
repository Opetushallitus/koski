package fi.oph.tor.schema

import fi.oph.tor.documentation.TorOppijaExamples
import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.annotation.{RegularExpression, MinValue, Description, ReadOnly}
import fi.oph.tor.schema.generic._

object TorSchema {
  private val metadataTypes: List[MetadataSupport] = List(Description, KoodistoUri, KoodistoKoodiarvo, ReadOnly, MinValue, RegularExpression, OksaUri)
  lazy val schemaFactory: SchemaFactory = SchemaFactory(metadataTypes)
  lazy val schema = schemaFactory.createSchema(classOf[TorOppija].getName).asInstanceOf[ClassSchema]
  lazy val schemaJson = SchemaToJson.toJsonSchema(schema)(metadataTypes)
  lazy val schemaJsonString = Json.write(schemaJson)
}
