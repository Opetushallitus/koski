package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{SchemaFactory, Metadata, MetadataSupport, ObjectWithMetadata}
import org.json4s.JsonAST.JObject

import scala.annotation.StaticAnnotation

object ReadOnly extends MetadataSupport {
  def apply(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = x.appendMetadata(List(ReadOnly(params.mkString(" "))))

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case ReadOnly(desc) => appendToDescription(obj, desc)
    case _ => obj
  }

  override def myAnnotationClass = classOf[ReadOnly]
}

case class ReadOnly(desc: String) extends StaticAnnotation with Metadata