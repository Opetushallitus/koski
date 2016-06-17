package fi.oph.koski.koski

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.{ContextualExtractor, Json}
import fi.oph.koski.koodisto.{KoodistoResolvingDeserializer, KoodistoViitePalvelu}
import fi.oph.koski.organisaatio.{OrganisaatioRepository, OrganisaatioResolvingDeserializer}
import org.json4s._

object ValidatingAndResolvingExtractor {
  /**
   *  Extracts object from json value, and validates/resolves all KoodistoKoodiViite objects on the way.
   */
  def extract[T](json: JValue, context: ValidationAndResolvingContext)(implicit mf: Manifest[T]): Either[HttpStatus, T] = {
    ContextualExtractor.extract[T, ValidationAndResolvingContext](json, context)(mf, Json.jsonFormats + KoodistoResolvingDeserializer + OrganisaatioResolvingDeserializer)
  }
}

case class ValidationAndResolvingContext(koodistoPalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository)



