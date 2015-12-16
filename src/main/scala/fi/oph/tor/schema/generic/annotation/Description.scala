package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport, ObjectWithMetadata, SchemaFactory}
import org.json4s.JsonAST.JObject

import scala.annotation.StaticAnnotation

object Description extends MetadataSupport {
  override val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], SchemaFactory), ObjectWithMetadata[_]] = {
    case (annotationClass, params, schema: ObjectWithMetadata[_], _) if (annotationClass == classOf[Description].getName) =>
      schema.appendMetadata(List(Description(params.mkString(" "))))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case Description(desc) => appendToDescription(obj, desc)
    case _ => obj
  }
}

case class Description(text: String) extends StaticAnnotation with Metadata