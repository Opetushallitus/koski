package fi.oph.tor.koodisto

import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.json.{ContextualExtractor, Json}
import fi.oph.tor.schema.{KoodistoKoodiViite, Deserializer}
import fi.oph.tor.log.Logging
import org.json4s.{JValue, TypeInfo, Formats}

object KoodistoResolvingDeserializer extends Deserializer[KoodistoKoodiViite] with Logging {
  private val TheClass = classOf[KoodistoKoodiViite]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), KoodistoKoodiViite] = {
    case (TypeInfo(TheClass, _), json) =>
      val viite = json.extract[KoodistoKoodiViite](Json.jsonFormats, Manifest.classType(TheClass))
      ContextualExtractor.getContext[{def koodistoPalvelu: KoodistoViitePalvelu}] match {
        case Some(context) =>
          val validated: Option[KoodistoKoodiViite] = try {
            context.koodistoPalvelu.validate(viite)
          } catch {
            case e: Exception =>
              logger.error("Error from koodisto-service", e)
              ContextualExtractor.extractionError(HttpStatus(TorErrorCategory.internalError))
          }
          validated match {
            case Some(viite) => viite
            case None =>
              ContextualExtractor.extractionError(HttpStatus(TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi, "Koodia " + viite + " ei löydy koodistosta"))
          }
        case _ => throw new RuntimeException("KoodistoResolvingDeserializer used without valid thread-local context")
      }
  }
}
