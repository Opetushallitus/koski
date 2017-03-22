package fi.oph.koski.koodisto

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{CustomDeserializer, DeserializationContext, Koodistokoodiviite, SchemaBasedJsonDeserializer}
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.scalaschema.{Metadata, SchemaWithClassName}
import org.json4s._

case class KoodistoResolvingCustomDeserializer(koodistoPalvelu: KoodistoViitePalvelu) extends CustomDeserializer with Logging {
  override def extract(json: JValue, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: DeserializationContext) = {
    val viite = SchemaBasedJsonDeserializer.extract(json, schema, metadata)(context.copy(customDeserializers = Nil))
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
            throw new InvalidRequestException(KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia " + viite + " ei löydy koodistosta"))
        }
      case errors => errors
    }
  }

  def isApplicable(schema: SchemaWithClassName): Boolean = schema.fullClassName == classOf[Koodistokoodiviite].getName
}
