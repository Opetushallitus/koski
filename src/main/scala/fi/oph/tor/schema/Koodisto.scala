package fi.oph.tor.schema

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport}
import org.json4s.JsonAST._

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
      val replacedObj = JObject(
        "type" -> JString("object"),
        "properties" -> JObject(
          "koodistoUri" -> JObject("type" -> JString("string"), "description" -> JString("Käytetyn koodiston tunniste"), "enum" -> JArray(List(JString(koodistoUri)))),
          "koodiarvo" -> JObject("type" -> JString("string"), "description" -> JString("Koodin tunniste koodistossa")),
          "nimi" -> JObject("type" -> JString("string"), "description" -> JString("Koodin selväkielinen, kielistetty nimi Tiedon syötössä kuvausta ei tarvita; kuvaus haetaan Koodistopalvelusta")),
          "koodistoVersio" -> JObject("type" -> JString("string"), "description" -> JString("Käytetyn koodiston versio. Jos versiota ei määritellä, käytetään uusinta versiota"))
        ),
        "additionalProperties" -> JBool(false),
        "required" -> JArray(List(JString("koodiarvo"), JString("koodistoUri")))
      )
      appendToDescription(replacedObj, "\n(Koodisto: " + koodistoUri + ")")
    case _ => obj
  }
}
