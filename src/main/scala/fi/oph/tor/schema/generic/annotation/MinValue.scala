package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport, ObjectWithMetadata, SchemaFactory}
import org.json4s.JsonAST.{JDouble, JObject}

import scala.annotation.StaticAnnotation

object MinValue extends MetadataSupport {
  override val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], SchemaFactory), ObjectWithMetadata[_]] = {
    case (annotationClass, params, schema: ObjectWithMetadata[_], _)  if (annotationClass == classOf[MinValue].getName) =>
      schema.appendMetadata(List(MinValue(params(0).toDouble)))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case MinValue(value) =>
      obj.merge(JObject("minimum" -> JDouble(value)))
    case _ => obj
  }
}

case class MinValue(value: Double) extends StaticAnnotation with Metadata
