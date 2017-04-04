package fi.oph.koski.organisaatio

import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.scalaschema.extraction.{CustomDeserializer, OtherViolation, ValidationError}
import fi.oph.scalaschema.{ExtractionContext, Metadata, SchemaValidatingExtractor, SchemaWithClassName}
import org.json4s._

case class OrganisaatioResolvingCustomDeserializer(organisaatioRepository: OrganisaatioRepository) extends CustomDeserializer with Logging {
  override def extract(json: JValue, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: ExtractionContext): Either[List[ValidationError], Any] = {
    SchemaValidatingExtractor.extract(json, schema, metadata)(context.copy(customDeserializers = Nil)) match {
      case Right(o: OrganisaatioWithOid) =>
        val c = Class.forName(schema.fullClassName)
        organisaatioRepository.getOrganisaatio(o.oid) match {
          case Some(org) if (c == classOf[OidOrganisaatio] || c.isInstance(org)) =>
            Right(org)
          case Some(org) =>
            Left(List(ValidationError(context.path, json, OtherViolation("Organisaatio " + o.oid + " ei ole " + c.getSimpleName.toLowerCase + " vaan " + org.getClass.getSimpleName.toLowerCase, "vääränTyyppinenOrganisaatio"))))
          case None =>
            Left(List(ValidationError(context.path, json, OtherViolation("Organisaatiota " + o.oid + " ei löydy organisaatiopalvelusta", "organisaatioTuntematon"))))
        }
      case Right(org: Organisaatio) => Right(org)
      case errors => errors
    }
  }

  def isApplicable(schema: SchemaWithClassName): Boolean = classOf[OrganisaatioWithOid].isAssignableFrom(Class.forName(schema.fullClassName))
}
