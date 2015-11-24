package fi.oph.tor.schema

import fi.oph.tor.schema.generic._
import org.json4s.JsonAST._

import scala.annotation.StaticAnnotation

/*
  Vain tietty koodiarvo hyväksytään -annotaatio
 */
case class KoodistoKoodiarvo(arvo: String) extends StaticAnnotation with Metadata {
}

object KoodistoKoodiarvo extends MetadataSupport {
  override val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], ScalaJsonSchema), ObjectWithMetadata[_]] = {
    case (annotationClass, params, property: Property, schema) if (annotationClass == classOf[KoodistoKoodiarvo].getName) =>
      val koodiarvo = KoodistoKoodiarvo(params.mkString(" "))
      val koodistoViiteType: ClassType = schema.createSchemaType(classOf[KoodistoKoodiViite].getName)
      val modifiedInnerType: SchemaType = koodistoViiteType.copy(properties = koodistoViiteType.properties.map{
        case p if p.key == "koodiarvo" => p.copy(tyep = StringType(enumValues = Some(List(koodiarvo.arvo))))
        case p => p
      })
      val finalInnerType = property.tyep.mapTo(modifiedInnerType)
      property.copy(tyep = finalInnerType).appendMetadata(List(koodiarvo))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case _ => obj
  }
}
