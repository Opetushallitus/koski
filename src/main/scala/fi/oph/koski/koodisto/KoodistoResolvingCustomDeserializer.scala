package fi.oph.koski.koodisto

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Koodistokoodiviite, PaikallinenKoodi}
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.scalaschema._
import fi.oph.scalaschema.extraction.{CustomDeserializer, OtherViolation, ValidationError}

case class KoodistoResolvingCustomDeserializer(koodistoPalvelu: KoodistoViitePalvelu) extends CustomDeserializer with Logging {
  override def extract(json: JsonCursor, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: ExtractionContext) = {
    val viite = SchemaValidatingExtractor.extract(json, schema, metadata)(context.copy(customDeserializers = Nil))
    viite match {
      case Right(viite: Koodistokoodiviite) =>
        val validated: Option[Koodistokoodiviite] = try {
          koodistoPalvelu.validate(viite)
        } catch {
          case e: Exception =>
            logger.error(e)("Error from koodisto-service")
            throw new InvalidRequestException(KoskiErrorCategory.internalError())
        }

        validated match {
          case Some(viite) =>
            Right(viite)
          case None =>
            Left(List(ValidationError(json.path, json.json, OtherViolation("Koodia " + viite + " ei löydy koodistosta", "tuntematonKoodi"))))
        }
      case Right(viite: PaikallinenKoodi) => Right(viite.copy(koodistoUri = None))
      case errors => errors
    }
  }

  def isApplicable(schema: SchemaWithClassName): Boolean = schema.appliesToClass(classOf[Koodistokoodiviite]) || schema.appliesToClass(classOf[PaikallinenKoodi])
}
