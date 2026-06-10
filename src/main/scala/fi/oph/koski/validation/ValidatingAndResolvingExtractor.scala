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
    val (customDeserializers, extractoitava) = if (deserializationContext.validate) {
      val cd = List(
        OrganisaatioResolvingCustomDeserializer(organisaatioRepository),
        KoodistoResolvingCustomDeserializer(koodistoPalvelu),
      )
      // @EiTallennetaOpiskeluoikeudenDataan-kentät (esim. lukuhetkellä täydennetty oppilaitostyyppi) eivät ole osa
      // syötettä. Poistetaan ennen validointia, jotta luku → muokkaus → tallennus -kierrätys (editori, fixturet,
      // tiedonsiirto) ei hylkää niitä ylimääräisinä kenttinä.
      (JaksoCustomDeserializer(cd) :: cd, fi.oph.koski.schema.KoskiSchema.poistaEiTallennettavatKentät(json))
    } else {
      (List(), json)
    }
    extract(extractoitava, deserializationContext.copy(customDeserializers = customDeserializers))
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
