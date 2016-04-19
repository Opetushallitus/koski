package fi.oph.tor.koodisto

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.json.{ContextualExtractor, Json}
import fi.oph.tor.log.Logging
import fi.oph.tor.schema.{Deserializer, Koodistokoodiviite}
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
              logger.error("Error from koodisto-service", e)
              ContextualExtractor.extractionError(TorErrorCategory.internalError())
          }
          validated match {
            case Some(viite) => viite
            case None =>
              ContextualExtractor.extractionError(TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia " + viite + " ei löydy koodistosta"))
          }
        case _ => throw new RuntimeException("KoodistoResolvingDeserializer used without valid thread-local context")
      }
  }
}
