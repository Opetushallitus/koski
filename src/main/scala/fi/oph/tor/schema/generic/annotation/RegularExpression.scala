package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport, ObjectWithMetadata, SchemaFactory}
import org.json4s.JsonAST.{JObject, JString}

import scala.annotation.StaticAnnotation

object RegularExpression extends MetadataSupport {
  override val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], SchemaFactory), ObjectWithMetadata[_]] = {
    case (annotationClass, params, schema: ObjectWithMetadata[_], _)  if (annotationClass == classOf[RegularExpression].getName) =>
      schema.appendMetadata(List(RegularExpression(params(0))))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case RegularExpression(pattern) =>
      appendToDescription(obj.merge(JObject("pattern" -> JString(pattern))), "(Muoto: " + pattern + ")")
    case _ => obj
  }
}

case class RegularExpression(pattern: String) extends StaticAnnotation with Metadata