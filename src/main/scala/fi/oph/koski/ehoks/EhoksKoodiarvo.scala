package fi.oph.koski.ehoks

import fi.oph.koski.schema.annotation.SchemaClassMapper.mapClasses
import fi.oph.scalaschema.{ClassSchema, Metadata, ObjectWithMetadata, Property, SchemaFactory}
import org.json4s.JsonAST.JObject

// KoodistoKoodiarvo only narrows Koodistokoodiviite, and the eHOKS response uses the slimmer EhoksKoodi
case class EhoksKoodiarvo(arvo: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject): JObject = obj

  override def applyMetadata(x: ObjectWithMetadata[_], schemaFactory: SchemaFactory): ObjectWithMetadata[_] =
    super.applyMetadata(mapClasses(x, schemaFactory, { case s: ClassSchema if s.fullClassName == classOf[EhoksKoodi].getName =>
      s.copy(properties = s.properties.map {
        case p: Property if p.key == "koodiarvo" => addEnumValue(arvo, p)
        case p => p
      })
    }), schemaFactory)
}
