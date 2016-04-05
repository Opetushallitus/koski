package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport, ObjectWithMetadata, SchemaFactory}
import org.json4s.JsonAST.{JDouble, JObject}

object MinValue extends MetadataSupport[MinValue] {
  def applyAnnotation(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = x.appendMetadata(List(MinValue(params(0).toDouble)))

  override def metadataClass = classOf[MinValue]

  override def appendMetadataToJsonSchema(obj: JObject, metadata: MinValue) = appendToDescription(obj.merge(JObject("minimum" -> JDouble(metadata.value))), "(Minimiarvo: " + metadata.value + ")")
}

case class MinValue(value: Double) extends Metadata
