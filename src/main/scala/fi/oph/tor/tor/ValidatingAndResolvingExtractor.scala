package fi.oph.tor.tor

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.{ContextualExtractor, Json}
import fi.oph.tor.koodisto.{KoodistoResolvingDeserializer, KoodistoPalvelu}
import fi.oph.tor.organisaatio.{OrganisaatioResolvingDeserializer, OrganisaatioRepository}
import org.json4s._

object ValidatingAndResolvingExtractor {
  /**
   *  Extracts object from json value, and validates/resolves all KoodistoKoodiViite objects on the way.
   */
  def extract[T](json: JValue, context: ValidationAndResolvingContext)(implicit mf: Manifest[T]): Either[HttpStatus, T] = {
    ContextualExtractor.extract[T, ValidationAndResolvingContext](json, context)(mf, Json.jsonFormats + KoodistoResolvingDeserializer + OrganisaatioResolvingDeserializer)
  }
}

case class ValidationAndResolvingContext(koodistoPalvelu: KoodistoPalvelu, organisaatioRepository: OrganisaatioRepository)



