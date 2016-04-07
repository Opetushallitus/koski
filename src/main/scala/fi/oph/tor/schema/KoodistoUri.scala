package fi.oph.tor.schema

import fi.oph.tor.schema.generic._
import org.json4s.JsonAST._

case class KoodistoUri(koodistoUri: String) extends Metadata {
  def asLink = <a href={"/tor/documentation/koodisto/" + koodistoUri + "/latest"} target="_blank">{koodistoUri}</a>
}

object KoodistoUri extends MetadataSupport[KoodistoUri] {
  override def applyMetadata(x: ObjectWithMetadata[_], koodistoUri: KoodistoUri, schemaFactory: SchemaFactory): ObjectWithMetadata[_] = x match {
    case property: Property =>
      val finalInnerSchema = property.schema.mapItems { itemSchema =>
        val koodistoViiteSchema: ClassSchema = toKoodistoKoodiViiteSchema(schemaFactory, itemSchema)
        koodistoViiteSchema.copy(properties = koodistoViiteSchema.properties.map{
          case p: Property if p.key == "koodistoUri" => addEnumValue(koodistoUri.koodistoUri, p)
          case p: Property => p
        })
      }
      property.copy(schema = finalInnerSchema).appendMetadata(List(koodistoUri))
    case x =>
      x
  }

  def toKoodistoKoodiViiteSchema(schemaFactory: SchemaFactory, itemSchema: Schema): ClassSchema = {
    val koodistoViiteSchema: ClassSchema = itemSchema match {
      case s: ClassSchema => s
      case s: ClassRefSchema => schemaFactory.createSchema(classOf[Koodistokoodiviite].getName).asInstanceOf[ClassSchema]
    }
    koodistoViiteSchema
  }

  override def metadataClass = classOf[KoodistoUri]

  override def appendMetadataToJsonSchema(obj: JObject, k: KoodistoUri) = appendToDescription(obj, "(Koodisto: " + k.asLink + ")")
}
