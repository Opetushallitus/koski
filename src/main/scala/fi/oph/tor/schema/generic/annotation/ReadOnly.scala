package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{SchemaFactory, Metadata, MetadataSupport, ObjectWithMetadata}
import org.json4s.JsonAST.JObject

import scala.annotation.StaticAnnotation

object ReadOnly extends MetadataSupport {
  override val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], SchemaFactory), ObjectWithMetadata[_]] = {
    case (annotationClass, params, schema: ObjectWithMetadata[_], _)  if (annotationClass == classOf[ReadOnly].getName) =>
      schema.appendMetadata(List(ReadOnly(params.mkString(" "))))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case ReadOnly(desc) =>
      appendToDescription(obj, "\n" + desc)
    case _ => obj
  }
}

case class ReadOnly(desc: String) extends StaticAnnotation with Metadata