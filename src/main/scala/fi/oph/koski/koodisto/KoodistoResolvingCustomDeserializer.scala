package fi.oph.koski.koodisto

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.scalaschema.extraction.{CustomDeserializer, OtherViolation, ValidationError}
import fi.oph.scalaschema.{ExtractionContext, Metadata, SchemaValidatingExtractor, SchemaWithClassName}
import org.json4s._

case class KoodistoResolvingCustomDeserializer(koodistoPalvelu: KoodistoViitePalvelu) extends CustomDeserializer with Logging {
  override def extract(json: JValue, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: ExtractionContext) = {
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
            Left(List(ValidationError(context.path, json, OtherViolation("Koodia " + viite + " ei löydy koodistosta", "tuntematonKoodi"))))
        }
      case errors => errors
    }
  }

  def isApplicable(schema: SchemaWithClassName): Boolean = schema.appliesToClass(classOf[Koodistokoodiviite])
}