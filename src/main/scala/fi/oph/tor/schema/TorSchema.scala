package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation._
import org.json4s.JObject

object TorSchema {
  private val metadataTypes: List[MetadataSupport[_]] = List(Description, KoodistoUri, KoodistoKoodiarvo, ReadOnly, MinValue, MinItems, MaxItems, RegularExpression, OksaUri)
  lazy val schemaFactory: SchemaFactory = SchemaFactory(metadataTypes)
  lazy val schema = schemaFactory.createSchema(classOf[Oppija].getName).asInstanceOf[ClassSchema]
  lazy val schemaJson = SchemaToJson.toJsonSchema(schema)(metadataTypes)
  lazy val schemaJsonString = Json.write(schemaJson)
}

object ReadOnly extends MetadataSupport[ReadOnly] {
  override def metadataClass = classOf[ReadOnly]
  override def appendMetadataToJsonSchema(obj: JObject, metadata: ReadOnly) = appendToDescription(obj, metadata.why)
}

case class ReadOnly(why: String) extends Metadata