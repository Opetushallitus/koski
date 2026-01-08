package fi.oph.koski.schema.annotation

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.{JBool, JObject}

// When field is deprecated it is hidden from the UI when it doesn't contain data
case class Deprecated(msg: String) extends Metadata {
  def descriptionWithStyle: String = "<b>Vanhentunut kenttä: </b>" + msg
  def wrapInParentheses(description: String) = s"($description)"
  override def appendMetadataToJsonSchema(obj: JObject) = appendToDescription(obj.merge(JObject("deprecated" -> JBool(true))), wrapInParentheses(descriptionWithStyle))
}
