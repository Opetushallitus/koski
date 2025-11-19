package fi.oph.koski.schema.annotation

import fi.oph.koski.schema.annotation.SchemaClassMapper.mapClasses
import fi.oph.scalaschema._
import org.json4s.JsonAST

case class KoodistoUri(koodistoUri: String) extends Metadata {
  def asLink: String = s"/koski/dokumentaatio/koodisto/$koodistoUri/latest"

  override def appendMetadataToJsonSchema(obj: JsonAST.JObject) = {
    appendToDescription(obj, s"(Koodisto: $asLink)")
  }

  override def applyMetadata(x: ObjectWithMetadata[_], schemaFactory: SchemaFactory) = {
    super.applyMetadata(mapClasses(x, schemaFactory, { case s: ClassSchema =>
      s.copy(properties = s.properties.map {
        case p: Property if p.key == "koodistoUri" => addEnumValue(koodistoUri, p)
        case p: Property => p
      })
    }), schemaFactory)
  }
}
