package fi.oph.tor.schema

import fi.oph.tor.schema.OksaUri.baseUrl
import fi.oph.tor.schema.generic.{SchemaFactory, Metadata, MetadataSupport, ObjectWithMetadata}
import org.json4s.JObject

import scala.annotation.StaticAnnotation

case class OksaUri(tunnus: String, käsite: String) extends StaticAnnotation with Metadata {
  def asLink = <a href={baseUrl + tunnus} target="_blank">{käsite}</a>
}

object OksaUri extends MetadataSupport {
  val baseUrl = "https://confluence.csc.fi/display/oppija/Opetus+ja+koulutussanasto+-+OKSA#Opetusjakoulutussanasto-OKSA-"

  def applyAnnotation(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = params match {
    case List(tunnus, käsite) => x.appendMetadata(List(OksaUri(tunnus, käsite)))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case o: OksaUri => appendToDescription(obj, "(Oksa: " + o.asLink + ")")
    case _ => obj
  }

  override def myAnnotationClass = classOf[OksaUri]
}

