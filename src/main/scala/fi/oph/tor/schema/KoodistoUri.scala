package fi.oph.tor.schema

import fi.oph.tor.schema.generic._
import org.json4s.JsonAST._

import scala.annotation.StaticAnnotation

case class KoodistoUri(koodistoUri: String) extends StaticAnnotation with Metadata {
  def asLink = <a href={"/tor/documentation/koodisto/" + koodistoUri + "/latest"} target="_blank">{koodistoUri}</a>
}

object KoodistoUri extends MetadataSupport {
  override val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], SchemaFactory), ObjectWithMetadata[_]] = {
    case (annotationClass, params, property: Property, schema) if (annotationClass == classOf[KoodistoUri].getName) =>
      val koodistoUri = KoodistoUri(params.mkString(" "))
      val koodistoViiteSchema: ClassSchema = schema.createSchema(classOf[KoodistoKoodiViite].getName)
      val modifiedInnerSchema: Schema = koodistoViiteSchema.copy(properties = koodistoViiteSchema.properties.map{
        case p if p.key == "koodistoUri" => p.copy(tyep = StringSchema(enumValues = Some(List(koodistoUri.koodistoUri))))
        case p => p
      })
      val finalInnerSchema = property.tyep.mapTo(modifiedInnerSchema)
      property.copy(tyep = finalInnerSchema).appendMetadata(List(koodistoUri))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case k: KoodistoUri =>
      appendToDescription(obj, "(Koodisto: " + k.asLink + ")")
    case _ => obj
  }
}
