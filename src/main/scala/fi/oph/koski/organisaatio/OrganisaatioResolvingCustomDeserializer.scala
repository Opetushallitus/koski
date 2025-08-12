package fi.oph.koski.organisaatio

import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.AllowKoulutustoimijaOidAsOppilaitos
import fi.oph.scalaschema._
import fi.oph.scalaschema.extraction.{CustomDeserializer, OtherViolation, ValidationError}

case class OrganisaatioResolvingCustomDeserializer(organisaatioRepository: OrganisaatioRepository) extends CustomDeserializer with Logging {
  override def extract(cursor: JsonCursor, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: ExtractionContext): Either[List[ValidationError], Any] = {
    SchemaValidatingExtractor.extract(cursor, schema, metadata)(context.copy(customDeserializers = Nil)) match {
      case Right(o: OrganisaatioWithOid) =>
        val c = Class.forName(schema.fullClassName)
        organisaatioRepository.getOrganisaatio(o.oid) match {
          case Some(org) if c == classOf[OidOrganisaatio] || c.isInstance(org) =>
            Right(org)
          case Some(org) if org.isInstanceOf[Koulutustoimija] && metadata.exists(_.isInstanceOf[AllowKoulutustoimijaOidAsOppilaitos]) =>
            Right(org.asInstanceOf[Koulutustoimija].toTuntematonOppilaitos)
          case Some(org) =>
            Left(List(ValidationError(cursor.path, cursor.json, OtherViolation("Organisaatio " + o.oid + " ei ole " + c.getSimpleName.toLowerCase + " vaan " + org.getClass.getSimpleName.toLowerCase, "vääränTyyppinenOrganisaatio"))))
          case None =>
            Left(List(ValidationError(cursor.path, cursor.json, OtherViolation("Organisaatiota " + o.oid + " ei löydy organisaatiopalvelusta", "organisaatioTuntematon"))))
        }
      case Right(org: Organisaatio) => Right(org)
      case errors => errors
    }
  }

  def isApplicable(schema: SchemaWithClassName): Boolean = classOf[OrganisaatioWithOid].isAssignableFrom(Class.forName(schema.fullClassName))
}
