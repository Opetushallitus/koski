package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic._
import fi.oph.tor.schema.generic.annotation.{Description, MinValue, ReadOnly, RegularExpression}

object TorSchema {
  private val metadataTypes: List[MetadataSupport[_]] = List(Description, KoodistoUri, KoodistoKoodiarvo, ReadOnly, MinValue, RegularExpression, OksaUri)
  lazy val schemaFactory: SchemaFactory = SchemaFactory(metadataTypes)
  lazy val schema = schemaFactory.createSchema(classOf[TorOppija].getName).asInstanceOf[ClassSchema]
  lazy val schemaJson = SchemaToJson.toJsonSchema(schema)(metadataTypes)
  lazy val schemaJsonString = Json.write(schemaJson)
}
