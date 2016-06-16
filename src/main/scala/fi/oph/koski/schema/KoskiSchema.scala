package fi.oph.koski.schema

import fi.oph.scalaschema._
import fi.oph.koski.json.Json
import org.json4s.{JsonAST, JObject}

object KoskiSchema {
  private val metadataTypes = SchemaFactory.defaultAnnotations ++ List(classOf[KoodistoUri], classOf[KoodistoKoodiarvo], classOf[ReadOnly], classOf[OksaUri])
  lazy val schemaFactory: SchemaFactory = SchemaFactory(metadataTypes)
  lazy val schema = schemaFactory.createSchema(classOf[Oppija].getName).asInstanceOf[ClassSchema].moveDefinitionsToTopLevel
  lazy val schemaJson = SchemaToJson.toJsonSchema(schema)
  lazy val schemaJsonString = Json.write(schemaJson)
}

case class ReadOnly(why: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JsonAST.JObject) = appendToDescription(obj, why)
}