package fi.oph.tor.schema

import fi.oph.tor.schema.KoodistoUri._
import fi.oph.tor.schema.generic._
import org.json4s.JsonAST._

import scala.annotation.StaticAnnotation

/*
  Vain tietty koodiarvo hyväksytään -annotaatio
 */
case class KoodistoKoodiarvo(arvo: String) extends StaticAnnotation with Metadata {
}

object KoodistoKoodiarvo extends MetadataSupport {
  override val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], SchemaFactory), ObjectWithMetadata[_]] = {
    case (annotationClass, params, property: Property, schemaFactory) if (annotationClass == classOf[KoodistoKoodiarvo].getName) =>
      val koodiarvo = KoodistoKoodiarvo(params.mkString(" "))

      val finalInnerSchema = property.schema.mapItems { itemSchema =>
        val koodistoViiteSchema: ClassSchema = toKoodistoKoodiViiteSchema(schemaFactory, itemSchema)
        koodistoViiteSchema.copy(properties = koodistoViiteSchema.properties.map{
          case p if p.key == "koodiarvo" => addEnumValue(koodiarvo.arvo, p)
          case p => p
        })
      }
      property.copy(schema = finalInnerSchema).appendMetadata(List(koodiarvo))

  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case _ => obj
  }
}
