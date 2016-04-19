package fi.oph.tor.tor

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.{ContextualExtractor, Json}
import fi.oph.tor.koodisto.{KoodistoResolvingDeserializer, KoodistoViitePalvelu}
import fi.oph.tor.localization.LocalizedStringValidatingDeserializer
import fi.oph.tor.organisaatio.{OrganisaatioResolvingDeserializer, OrganisaatioRepository}
import org.json4s._

object ValidatingAndResolvingExtractor {
  /**
   *  Extracts object from json value, and validates/resolves all KoodistoKoodiViite objects on the way.
   */
  def extract[T](json: JValue, context: ValidationAndResolvingContext)(implicit mf: Manifest[T]): Either[HttpStatus, T] = {
    ContextualExtractor.extract[T, ValidationAndResolvingContext](json, context)(mf, Json.jsonFormats + KoodistoResolvingDeserializer + OrganisaatioResolvingDeserializer + LocalizedStringValidatingDeserializer)
  }
}

case class ValidationAndResolvingContext(koodistoPalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository)



