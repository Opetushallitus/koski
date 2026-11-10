package fi.oph.koski.schema.annotation

import fi.oph.scalaschema.RepresentationalMetadata
import org.json4s.JsonAST.{JBool, JObject}

/* Kenttä jätetään pois aiemmatOpiskeluoikeudet-massaluovutuskyselyn tuloksista käyttöoikeuksista riippumatta.
   Eri asia kuin SensitiveData: tieto ei ole arkaluontoinen, mutta sen ei ole katsottu kuuluvan uudelle oppilaitokselle. */
case class HiddenFromAiemmatOpiskeluoikeudet() extends RepresentationalMetadata {
  def descriptionWithStyle: String = "<b>Ei palauteta aiemmatOpiskeluoikeudet-massaluovutuskyselyssä.</b>"
  def wrapInParentheses(description: String) = s"($description)"
  override def appendMetadataToJsonSchema(obj: JObject) = appendToDescription(obj.merge(JObject("hiddenFromAiemmatOpiskeluoikeudet" -> JBool(true))), wrapInParentheses(descriptionWithStyle))
}
