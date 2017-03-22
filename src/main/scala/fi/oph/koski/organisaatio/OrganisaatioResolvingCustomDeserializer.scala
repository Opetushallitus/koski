package fi.oph.koski.organisaatio

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.scalaschema.{Metadata, SchemaWithClassName}
import org.json4s._

case class OrganisaatioResolvingCustomDeserializer(organisaatioRepository: OrganisaatioRepository) extends CustomDeserializer with Logging {
  override def extract(json: JValue, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: DeserializationContext) = {
    SchemaBasedJsonDeserializer.extract(json, schema, metadata)(context.copy(customDeserializers = Nil)) match {
      case Right(o: OrganisaatioWithOid) =>
        val c = Class.forName(schema.fullClassName)
        organisaatioRepository.getOrganisaatio(o.oid) match {
          case Some(org) if (c == classOf[OidOrganisaatio] || c.isInstance(org)) =>
            Right(org)
          case Some(org) =>
            throw new InvalidRequestException(KoskiErrorCategory.badRequest.validation.organisaatio.vääränTyyppinen("Organisaatio " + o.oid + " ei ole " + c.getSimpleName.toLowerCase + " vaan " + org.getClass.getSimpleName.toLowerCase))
          case None =>
            throw new InvalidRequestException(KoskiErrorCategory.badRequest.validation.organisaatio.tuntematon("Organisaatiota " + o.oid + " ei löydy organisaatiopalvelusta"))
        }
      case Right(org: Organisaatio) => Right(org)
      case errors => errors
    }
  }

  def isApplicable(schema: SchemaWithClassName): Boolean = classOf[OrganisaatioWithOid].isAssignableFrom(Class.forName(schema.fullClassName))
}
