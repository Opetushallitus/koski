package fi.oph.koski.koodisto

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{ContextualExtractor, Json}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Deserializer, Koodistokoodiviite}
import org.json4s.{Formats, JValue, TypeInfo}

object KoodistoResolvingDeserializer extends Deserializer[Koodistokoodiviite] with Logging {
  private val TheClass = classOf[Koodistokoodiviite]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Koodistokoodiviite] = {
    case (TypeInfo(TheClass, _), json) =>
      val viite = json.extract[Koodistokoodiviite](Json.jsonFormats, Manifest.classType(TheClass))
      ContextualExtractor.getContext[{def koodistoPalvelu: KoodistoViitePalvelu}] match {
        case Some(context) =>
          val validated: Option[Koodistokoodiviite] = try {
            context.koodistoPalvelu.validate(viite)
          } catch {
            case e: Exception =>
              logger.error(e)("Error from koodisto-service")
              ContextualExtractor.extractionError(KoskiErrorCategory.internalError())
          }
          validated match {
            case Some(viite) => viite
            case None =>
              ContextualExtractor.extractionError(KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia " + viite + " ei löydy koodistosta"))
          }
        case _ => throw new RuntimeException("KoodistoResolvingDeserializer used without valid thread-local context")
      }
  }
}