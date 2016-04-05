package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport, ObjectWithMetadata, SchemaFactory}
import org.json4s.JsonAST.{JDouble, JObject}

import scala.annotation.StaticAnnotation

object MinValue extends MetadataSupport {
  def apply(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = x.appendMetadata(List(MinValue(params(0).toDouble)))

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case MinValue(value) =>
      appendToDescription(obj.merge(JObject("minimum" -> JDouble(value))), "(Minimiarvo: " + value + ")")
    case _ => obj
  }

  override def myAnnotationClass = classOf[MinValue]
}

case class MinValue(value: Double) extends StaticAnnotation with Metadata
