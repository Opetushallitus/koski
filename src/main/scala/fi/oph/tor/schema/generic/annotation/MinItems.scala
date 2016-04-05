package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport}
import org.json4s.JsonAST.{JInt, JObject}

object MinItems extends MetadataSupport[MinItems] {
  override def metadataClass = classOf[MinItems]

  override def appendMetadataToJsonSchema(obj: JObject, metadata: MinItems) = appendToDescription(obj.merge(JObject("minItems" -> JInt(metadata.value))), "(Arvoja vähintään: " + metadata.value + ")")
}

case class MinItems(value: Int) extends Metadata