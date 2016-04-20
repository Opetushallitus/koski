package fi.oph.tor.schema

import fi.oph.tor.schema.KoodistoUri._
import fi.oph.tor.schema.generic._
import org.json4s.JsonAST._

/*
  Vain tietty koodiarvo hyväksytään -annotaatio
 */
case class KoodistoKoodiarvo(arvo: String) extends Metadata

object KoodistoKoodiarvo extends MetadataSupport[KoodistoKoodiarvo] {
  override def applyMetadata(x: ObjectWithMetadata[_], koodiarvo: KoodistoKoodiarvo, schemaFactory: SchemaFactory): ObjectWithMetadata[_] = x match {
    case property: Property =>
      val finalInnerSchema = property.schema.mapItems { itemSchema =>
        val koodistoViiteSchema: ClassSchema = toKoodistoKoodiViiteSchema(schemaFactory, itemSchema)
        koodistoViiteSchema.copy(properties = koodistoViiteSchema.properties.map{
          case p: Property if p.key == "koodiarvo" => addEnumValue(koodiarvo.arvo, p)
          case p: Property => p
        })
      }
      property.copy(schema = finalInnerSchema).appendMetadata(List(koodiarvo))
    case x => x
  }

  override def metadataClass = classOf[KoodistoKoodiarvo]

  override def appendMetadataToJsonSchema(obj: JObject, k: KoodistoKoodiarvo) = appendToDescription(obj, "(Hyväksytty koodiarvo: " + k.arvo + ")")
}
