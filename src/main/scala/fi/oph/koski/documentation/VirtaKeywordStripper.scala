package fi.oph.koski.documentation

import fi.oph.koski.schema.annotation.{VirtaDerived, VirtaNote, VirtaSource}
import fi.oph.scalaschema.{ClassSchema, Metadata, Property, SchemaJsonDecorator}
import org.json4s.JsonAST.{JObject, JString, JValue}

// Käytössä, kun features.virtaSchemaDocumentation on pois päältä.
class VirtaKeywordStripper(inner: SchemaJsonDecorator) extends SchemaJsonDecorator {
  override def decorateClass(schema: ClassSchema, json: JObject): JObject = inner.decorateClass(schema, json)

  // Kuvaus muodostetaan uudelleen ilman Virta-annotaatioita samoin kuin SchemaToJson sen kokoaa:
  // valmiista merkkijonosta ei voi päätellä, päättyikö alkuperäinen kuvaus pisteeseen.
  override def decorateProperty(property: Property, json: JObject): JObject = {
    val description = (property.metadata ++ property.schema.metadata)
      .filterNot(isVirta)
      .foldLeft(JObject())((o, m) => m.appendMetadataToJsonSchema(o)) \ "description"
    val fields = json.obj.flatMap {
      case ("virta", _) => None
      case ("description", _) => description match {
        case s: JString => Some("description" -> (s: JValue))
        case _ => None
      }
      case other => Some(other)
    }
    inner.decorateProperty(property, JObject(fields))
  }

  private def isVirta(m: Metadata): Boolean =
    m.isInstanceOf[VirtaSource] || m.isInstanceOf[VirtaDerived] || m.isInstanceOf[VirtaNote]
}
