package fi.oph.koski.schema.annotation

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.JObject

case class OksaUri(tunnus: String, käsite: String) extends Metadata {
  private val baseUrl = "https://wiki.eduuni.fi/display/ophoppija/Opetus+ja+koulutussanasto+-+OKSA"
  def asLink = <a href={baseUrl + "#" + tunnus} target="_blank">{käsite}</a>

  override def appendMetadataToJsonSchema(obj: JObject) = appendToDescription(obj, s"(Oksa: $asLink)")
}
