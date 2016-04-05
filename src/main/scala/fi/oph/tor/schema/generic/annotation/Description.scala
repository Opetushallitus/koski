package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport, ObjectWithMetadata, SchemaFactory}
import org.json4s.JsonAST.JObject

import scala.annotation.StaticAnnotation

object Description extends MetadataSupport {
  def applyAnnotation(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = x.appendMetadata(List(Description(params.mkString(" "))))

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case Description(desc) => appendToDescription(obj, desc)
    case _ => obj
  }

  override def myAnnotationClass = classOf[Description]
}

case class Description(text: String) extends StaticAnnotation with Metadata