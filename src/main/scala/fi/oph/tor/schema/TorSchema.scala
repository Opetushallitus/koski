package fi.oph.tor.schema

import fi.oph.scalaschema._
import fi.oph.tor.json.Json
import org.json4s.{JsonAST, JObject}

object TorSchema {
  private val metadataTypes = SchemaFactory.defaultAnnotations ++ List(classOf[KoodistoUri], classOf[KoodistoKoodiarvo], classOf[ReadOnly], classOf[OksaUri])
  lazy val schemaFactory: SchemaFactory = SchemaFactory(metadataTypes)
  lazy val schema = schemaFactory.createSchema(classOf[Oppija].getName).asInstanceOf[ClassSchema]
  lazy val schemaJson = SchemaToJson.toJsonSchema(schema)
  lazy val schemaJsonString = Json.write(schemaJson)
}

case class ReadOnly(why: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JsonAST.JObject) = appendToDescription(obj, why)
}