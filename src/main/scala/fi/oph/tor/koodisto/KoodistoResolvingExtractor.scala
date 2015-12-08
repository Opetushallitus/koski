package fi.oph.tor.koodisto

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.{ContextualExtractor, Json}
import fi.oph.tor.schema._
import fi.vm.sade.utils.slf4j.Logging
import org.json4s._
import org.json4s.reflect.TypeInfo

object KoodistoResolvingExtractor {
  /**
   *  Extracts object from json value, and validates/resolves all KoodistoKoodiViite objects on the way.
   */
  def extract[T](json: JValue)(implicit mf: Manifest[T], koodistoPalvelu: KoodistoPalvelu): Either[HttpStatus, T] = {
    ContextualExtractor.extract[T, KoodistoPalvelu](json, koodistoPalvelu)(mf, Json.jsonFormats + KoodistoResolvingDeserializer)
  }

  private object KoodistoResolvingDeserializer extends Deserializer[KoodistoKoodiViite] with Logging {
    private val TheClass = classOf[KoodistoKoodiViite]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), KoodistoKoodiViite] = {
      case (TypeInfo(TheClass, _), json) =>
        val viite = json.extract[KoodistoKoodiViite](Json.jsonFormats, Manifest.classType(TheClass))
        ContextualExtractor.getContext[KoodistoPalvelu] match {
          case Some(koodistoPalvelu) =>
            val validated: Option[KoodistoKoodiViite] = try {
              koodistoPalvelu.validate(viite)
            } catch {
              case e: Exception =>
                logger.error("Error from koodisto-service", e)
                ContextualExtractor.extractionError(HttpStatus.internalError())
            }
            validated match {
              case Some(viite) => viite
              case None =>
                ContextualExtractor.extractionError(HttpStatus.badRequest("Koodia " + viite + " ei löydy koodistosta"))
            }
          case _ => viite
        }
    }
  }
}

