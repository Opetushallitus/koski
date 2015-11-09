package fi.oph.tor.schema

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport}
import org.json4s.JsonAST.JObject

import scala.annotation.StaticAnnotation

case class KoodistoUri(koodistoUri: String) extends StaticAnnotation with Metadata {
}

object KoodistoUri extends MetadataSupport {
  override val extractMetadata: PartialFunction[(String, List[String]), List[Metadata]] = {
    case (annotationClass, params) if (annotationClass == classOf[KoodistoUri].getName) =>
      List(KoodistoUri(params.mkString(" ")))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case KoodistoUri(koodistoUri) =>
      // TODO: tänne hyperlinkki koodistoon
      appendToDescription(obj, "\n(Koodisto: " + koodistoUri + ")")
    case _ => obj
  }
}
