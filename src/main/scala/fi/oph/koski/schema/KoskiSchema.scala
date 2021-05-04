package fi.oph.koski.schema

import fi.oph.scalaschema._
import org.json4s.JValue
import org.json4s.jackson.JsonMethods

object KoskiSchema {
  lazy val schemaFactory: SchemaFactory = SchemaFactory()
  lazy val schema = createSchema(classOf[Oppija]).asInstanceOf[ClassSchema]
  lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(schema)
  lazy val schemaJsonString = JsonMethods.compact(schemaJson)
  lazy implicit val deserializationContext = ExtractionContext(schemaFactory, allowEmptyStrings = false)

  def createSchema(clazz: Class[_]) = schemaFactory.createSchema(clazz) match {
    case s: AnyOfSchema => s
    case s: ClassSchema => s.moveDefinitionsToTopLevel
    case _ => ???
  }

  def skipSyntheticProperties(s: ClassSchema, p: Property): List[Property] = if (p.synthetic) Nil else List(p)
}
