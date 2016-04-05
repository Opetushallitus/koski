package fi.oph.tor.schema

import fi.oph.tor.schema.KoodistoUri._
import fi.oph.tor.schema.generic._
import org.json4s.JsonAST._

import scala.annotation.StaticAnnotation

/*
  Vain tietty koodiarvo hyväksytään -annotaatio
 */
case class KoodistoKoodiarvo(arvo: String) extends StaticAnnotation with Metadata

object KoodistoKoodiarvo extends MetadataSupport {
  def applyAnnotation(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = x match {
    case property: Property =>
      val koodiarvo = KoodistoKoodiarvo(params.mkString(" "))

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

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case _ => obj
  }

  override def myAnnotationClass = classOf[KoodistoKoodiarvo]
}
