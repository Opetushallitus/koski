package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._
import org.json4s._

object ValidatingAndResolvingExtractor {
  /**
   *  Extracts object from json value, and validates/resolves all KoodistoKoodiViite objects on the way.
   */
  def extract[T](json: JValue, context: ValidationAndResolvingContext)(implicit mf: Manifest[T]): Either[HttpStatus, T] = {
    //ContextualExtractor.extract[T, ValidationAndResolvingContext](json, context)(mf, Json.jsonFormats + KoodistoResolvingDeserializer + OrganisaatioResolvingDeserializer)
    val klass = mf.asInstanceOf[ClassManifest[_]].runtimeClass
    SchemaBasedJsonDeserializer.extract(json, KoskiSchema.schema.getSchema(klass.getName).get)(DeserializationContext(KoskiSchema.schema, customDeserializers = List(
      (classOf[Organisaatio].getName, OrganisaatioResolvingCustomDeserializer(context.organisaatioRepository)), // TODO: this should match to all OrganisaatioWithOid subtypes
      (classOf[Koodistokoodiviite].getName, KoodistoResolvingCustomDeserializer(context.koodistoPalvelu))
    ))) match {
      case Right(t: T) => Right(t)
      case Left(errors) => Left(KoskiErrorCategory.badRequest.validation.jsonSchema(errors))
    }
  }
}

case class ValidationAndResolvingContext(koodistoPalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository)



