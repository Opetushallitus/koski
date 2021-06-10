package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.koodisto.{KoodistoResolvingCustomDeserializer, KoodistoViitePalvelu}
import fi.oph.koski.organisaatio.{OrganisaatioRepository, OrganisaatioResolvingCustomDeserializer}
import fi.oph.koski.schema.JaksoCustomDeserializer
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import fi.oph.scalaschema.extraction.ValidationError
import org.json4s._

import scala.reflect.runtime.universe.TypeTag

class ValidatingAndResolvingExtractor(
  koodistoPalvelu: KoodistoViitePalvelu,
  organisaatioRepository: OrganisaatioRepository
) {
  /**
   * Extracts object from json value, and validates/resolves all KoodistoKoodiViite objects on the way.
   */
  def extract[T](deserializationContext: ExtractionContext)(json: JValue)(implicit tag: TypeTag[T])
  : Either[HttpStatus, T] = {
    val customDeserializers = List(
      OrganisaatioResolvingCustomDeserializer(organisaatioRepository),
      KoodistoResolvingCustomDeserializer(koodistoPalvelu)
    )
    extract(json, deserializationContext.copy(
      customDeserializers = JaksoCustomDeserializer(customDeserializers) :: customDeserializers
    ))
  }

  def extract[T](json: JValue, deserializationContext: ExtractionContext)(implicit tag: TypeTag[T]): Either[HttpStatus, T] = {
    SchemaValidatingExtractor.extract(json)(deserializationContext, tag) match {
      case Right(t) => Right(t)
      case Left(errors: List[ValidationError]) => Left(KoskiErrorCategory.badRequest.validation.jsonSchema.apply(JsonErrorMessage(errors)))
    }
  }
}
