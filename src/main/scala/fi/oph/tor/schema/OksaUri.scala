package fi.oph.tor.schema

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.JObject

case class OksaUri(tunnus: String, käsite: String) extends Metadata {
  def asLink = {
    val baseUrl = "https://confluence.csc.fi/display/oppija/Opetus+ja+koulutussanasto+-+OKSA#Opetusjakoulutussanasto-OKSA-"
    <a href={baseUrl + tunnus} target="_blank">{käsite}</a>
  }

  override def appendMetadataToJsonSchema(obj: JObject) = appendToDescription(obj, "(Oksa: " + asLink + ")")
}