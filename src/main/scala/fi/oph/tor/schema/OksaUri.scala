package fi.oph.tor.schema

import fi.oph.tor.schema.OksaUri.baseUrl
import fi.oph.tor.schema.generic.{Metadata, MetadataSupport, ObjectWithMetadata, SchemaFactory}
import org.json4s.JsonAST

case class OksaUri(tunnus: String, käsite: String) extends Metadata {
  def asLink = <a href={baseUrl + tunnus} target="_blank">{käsite}</a>
}

object OksaUri extends MetadataSupport[OksaUri] {
  val baseUrl = "https://confluence.csc.fi/display/oppija/Opetus+ja+koulutussanasto+-+OKSA#Opetusjakoulutussanasto-OKSA-"

  def applyAnnotation(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = params match {
    case List(tunnus, käsite) => x.appendMetadata(List(OksaUri(tunnus, käsite)))
  }

  def appendMetadataToJsonSchema(obj: JsonAST.JObject, o: OksaUri) = appendToDescription(obj, "(Oksa: " + o.asLink + ")")

  def metadataClass = classOf[OksaUri]
}

