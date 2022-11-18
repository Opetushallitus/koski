package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.koodisto.{KoodistoResolvingCustomDeserializer, KoodistoViitePalvelu}
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.{OrganisaatioRepository, OrganisaatioResolvingCustomDeserializer}
import fi.oph.koski.schema.JaksoCustomDeserializer
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import fi.oph.scalaschema.extraction.ValidationError
import org.json4s._

import scala.reflect.runtime.universe.TypeTag
import scala.util.{Failure, Success, Try}

class ValidatingAndResolvingExtractor(
  koodistoPalvelu: KoodistoViitePalvelu,
  organisaatioRepository: OrganisaatioRepository
) extends Logging {
  /**
   * Extracts object from json value, and validates/resolves all KoodistoKoodiViite objects on the way.
   */
  def extract[T](deserializationContext: ExtractionContext)(json: JValue)(implicit tag: TypeTag[T])
  : Either[HttpStatus, T] = {
    val customDeserializers = if (deserializationContext.validate) {
      List(
        OrganisaatioResolvingCustomDeserializer(organisaatioRepository),
        KoodistoResolvingCustomDeserializer(koodistoPalvelu),
      )
    } else {
      List()
    }
    extract(json, deserializationContext.copy(
      customDeserializers = JaksoCustomDeserializer(customDeserializers) :: customDeserializers
    ))
  }

  def extract[T](json: JValue, deserializationContext: ExtractionContext)(implicit tag: TypeTag[T]): Either[HttpStatus, T] = {
    Try(SchemaValidatingExtractor.extract(json)(deserializationContext, tag)) match {
      case Success(value) => value match {
        case Right(t) => Right(t)
        // TODO: Validaatiovirhe ei aina tarkoita bad request -virhettä koska virhe
        //  voi olla sisäisessä datan käsittelyssä. Refaktoroi tämä palauttamaan
        //  kaikki validaatiovirheet ja muuta se HttpStatukseksi muualla.
        case Left(errors: List[ValidationError]) => Left(KoskiErrorCategory.badRequest.validation.jsonSchema.apply(JsonErrorMessage(errors)))
      }
      case Failure(exception) =>
        logger.error(exception)("Unexpected exception during deserialization")
        Left(KoskiErrorCategory.internalError())
    }
  }
}
