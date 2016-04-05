package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport, ObjectWithMetadata, SchemaFactory}
import org.json4s.JsonAST.JObject

object ReadOnly extends MetadataSupport[ReadOnly] {
  def applyAnnotation(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = x.appendMetadata(List(ReadOnly(params.mkString(" "))))

  override def metadataClass = classOf[ReadOnly]

  override def appendMetadataToJsonSchema(obj: JObject, metadata: ReadOnly) = appendToDescription(obj, metadata.desc)
}

case class ReadOnly(desc: String) extends Metadata