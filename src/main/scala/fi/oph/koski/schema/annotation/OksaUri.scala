package fi.oph.koski.schema.annotation

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.{JObject, JString}

case class OksaUri(tunnus: String, käsite: String) extends Metadata {
  private val baseUrl = "https://wiki.eduuni.fi/display/ophoppija/Opetus+ja+koulutussanasto+-+OKSA"
  private val url = baseUrl + "#" + tunnus
  def asLink = <a href={url} target="_blank">{käsite}</a>

  override def appendMetadataToJsonSchema(obj: JObject) =
    appendToDescription(obj.merge(JObject("oksa" -> JObject("käsite" -> JString(käsite), "url" -> JString(url)))), s"(Oksa: $asLink)")
}
