package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport, ObjectWithMetadata, SchemaFactory}
import org.json4s.JsonAST.{JObject, JString}

object RegularExpression extends MetadataSupport[RegularExpression] {
  def applyAnnotation(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = x.appendMetadata(List(RegularExpression(params(0))))

  override def metadataClass = classOf[RegularExpression]

  override def appendMetadataToJsonSchema(obj: JObject, metadata: RegularExpression) = appendToDescription(obj.merge(JObject("pattern" -> JString(metadata.pattern))), "(Muoto: " + metadata.pattern + ")")
}

case class RegularExpression(pattern: String) extends Metadata