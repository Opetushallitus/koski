package fi.oph.tor.schema

import fi.oph.tor.schema.generic._
import org.json4s.JsonAST._

import scala.annotation.StaticAnnotation

case class KoodistoUri(koodistoUri: String) extends StaticAnnotation with Metadata {
}

object KoodistoUri extends MetadataSupport {
  override val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], ScalaJsonSchema), ObjectWithMetadata[_]] = {
    case (annotationClass, params, property: Property, schema) if (annotationClass == classOf[KoodistoUri].getName) =>
      val koodistoUri = KoodistoUri(params.mkString(" "))
      val koodistoViiteType: ClassType = schema.createSchemaType(classOf[KoodistoKoodiViite].getName)
      val modifiedInnerType: SchemaType = koodistoViiteType.copy(properties = koodistoViiteType.properties.map{
        case p if p.key == "koodistoUri" => p.copy(tyep = StringType(enumValues = Some(List(koodistoUri.koodistoUri))))
        case p => p
      })
      val finalInnerType = property.tyep.mapTo(modifiedInnerType)
      property.copy(tyep = finalInnerType).appendMetadata(List(koodistoUri))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case KoodistoUri(koodistoUri) =>
      appendToDescription(obj, "\n(Koodisto: " + koodistoUri + ")")
    case _ => obj
  }
}
