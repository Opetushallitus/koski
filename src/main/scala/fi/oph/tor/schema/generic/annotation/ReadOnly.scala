package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport}
import org.json4s.JsonAST.JObject

object ReadOnly extends MetadataSupport[ReadOnly] {
  override def metadataClass = classOf[ReadOnly]

  override def appendMetadataToJsonSchema(obj: JObject, metadata: ReadOnly) = appendToDescription(obj, metadata.desc)
}

case class ReadOnly(desc: String) extends Metadata