package fi.oph.koski.schema.annotation

import fi.oph.scalaschema.RepresentationalMetadata
import org.json4s.JsonAST.{JBool, JObject}

/* Handled similarly to SensitiveData, but fully omitted in serialization */
case class RedundantData() extends RepresentationalMetadata {
  def descriptionWithStyle: String = "<b>Kenttä ei ole käytössä.</b> Koski ei ota vastaan kentässä siirrettyä tietoa."
  def wrapInParentheses(description: String) = s"($description)"
  override def appendMetadataToJsonSchema(obj: JObject) = appendToDescription(obj.merge(JObject("redundantData" -> JBool(true))), wrapInParentheses(descriptionWithStyle))
}
