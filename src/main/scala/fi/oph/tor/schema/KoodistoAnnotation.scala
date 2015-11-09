package fi.oph.tor.schema

import fi.oph.tor.schema.generic.{MetadataSupport, Metadata}
import org.json4s.JsonAST
import org.json4s.JsonAST.{JNothing, JObject, JString}

import scala.annotation.StaticAnnotation

case class KoodistoAnnotation(koodistoNimi: String) extends StaticAnnotation with Metadata {
}

object KoodistoAnnotation extends MetadataSupport {
  override val extractMetadata: PartialFunction[(String, List[String]), List[Metadata]] = {
    case (annotationClass, params) if (annotationClass == classOf[KoodistoAnnotation].getName) =>
      List(KoodistoAnnotation(params.mkString(" ")))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case KoodistoAnnotation(koodistoUri) =>
      // TODO: tänne hyperlinkki koodistoon
      appendToDescription(obj, "\n(Koodisto: " + koodistoUri + ")")
    case _ => obj
  }
}
