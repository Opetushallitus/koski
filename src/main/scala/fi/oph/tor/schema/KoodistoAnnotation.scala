package fi.oph.tor.schema

import fi.oph.tor.schema.generic.{MetadataSupport, Metadata}
import org.json4s.JsonAST.{JNothing, JObject, JString}

import scala.annotation.StaticAnnotation

case class KoodistoAnnotation(koodistoNimi: String) extends StaticAnnotation with Metadata {
}

object KoodistoAnnotation extends MetadataSupport {
  override val extractMetadata: PartialFunction[(String, List[String]), List[Metadata]] = {
    case (annotationClass, params) if (annotationClass == classOf[KoodistoAnnotation].getName) =>
      List(KoodistoAnnotation(params.mkString(" ")))
  }

  override def appendMetadata(obj: JObject, metadata: Metadata) = metadata match {
    case KoodistoAnnotation(koodistoUri) =>
      val description = obj.\("description") match {
        case JString(s) => s
        case JNothing => ""
      }
      // TODO: tänne hyperlinkki koodistoon
      obj.merge(JObject("description" -> JString(description + "\n(Koodisto: " + koodistoUri + ")")))
    case _ => obj
  }
}
