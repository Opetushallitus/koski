package fi.oph.koski.schema

import fi.oph.koski.json.Json
import fi.oph.scalaschema._
import org.json4s._

object KoskiSchema {
  private val metadataTypes = SchemaFactory.defaultAnnotations ++ List(classOf[KoodistoUri], classOf[KoodistoKoodiarvo], classOf[ReadOnly], classOf[OksaUri], classOf[Hidden], classOf[Representative], classOf[ComplexObject], classOf[Flatten], classOf[Tabular], classOf[Discriminator])
  lazy val schemaFactory: SchemaFactory = SchemaFactory(metadataTypes)
  lazy val schema = createSchema(classOf[Oppija])
  lazy val schemaJson = SchemaToJson.toJsonSchema(schema)
  lazy val schemaJsonString = Json.write(schemaJson)

  def createSchema(clazz: Class[_]) = schemaFactory.createSchema(clazz) match {
    case s: AnyOfSchema => s
    case s: ClassSchema => s.moveDefinitionsToTopLevel
  }
}