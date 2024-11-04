package fi.oph.koski.schema.annotation

import fi.oph.scalaschema.{Metadata, ValueConversion}
import org.json4s.JValue
import org.json4s.JsonAST.JObject

/**
  * Flag for hiding properties from the UI
  */
case class Hidden() extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject) = obj
}

case class HiddenWhen(path: String, value: Any) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject) = obj
  def serializableForm = SerializableHiddenWhen(path, ValueConversion.anyToJValue(value))
}

case class SerializableHiddenWhen(path: String, value: JValue)
