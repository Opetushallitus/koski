package fi.oph.tor.schema

import fi.oph.tor.schema.OksaUri.baseUrl
import fi.oph.tor.schema.generic.{ScalaJsonSchema, Metadata, MetadataSupport, ObjectWithMetadata}
import org.json4s.JObject

import scala.annotation.StaticAnnotation

case class OksaUri(tunnus: String, käsite: String) extends StaticAnnotation with Metadata {
  def asLink = <a href={baseUrl + tunnus}>{käsite}</a>
}

object OksaUri extends MetadataSupport {

  val baseUrl = "https://confluence.csc.fi/display/oppija/Opetus+ja+koulutussanasto+-+OKSA#Opetusjakoulutussanasto-OKSA-"

  val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], ScalaJsonSchema), ObjectWithMetadata[_]] = {
    case (annotationClass, List(tunnus, käsite), schemaType: ObjectWithMetadata[_], _)  if (annotationClass == classOf[OksaUri].getName) =>
      schemaType.appendMetadata(List(OksaUri(tunnus, käsite)))
  }
  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case (o @ OksaUri(tunnus, käsite)) =>
      appendToDescription(obj, "\n(Oksa: " + o.asLink + ")")
    case _ => obj
  }
}

