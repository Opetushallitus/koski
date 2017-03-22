package fi.oph.koski.organisaatio

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{ContextualExtractor, Json}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.scalaschema.Schema
import org.json4s._
import org.json4s.reflect.TypeInfo

object OrganisaatioResolvingDeserializer extends Deserializer[Organisaatio] with Logging {
  val OppilaitosClass = classOf[Oppilaitos]
  val organisaatioClasses = List(classOf[Organisaatio], classOf[OrganisaatioWithOid], classOf[Oppilaitos], classOf[OidOrganisaatio], classOf[Toimipiste], classOf[Koulutustoimija], classOf[Tutkintotoimikunta], classOf[Yritys])

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Organisaatio] = {
    case (TypeInfo(c, _), json) if organisaatioClasses.contains(c) =>
      OrganisaatioDeserializer.deserialize(Json.jsonFormats)((TypeInfo(classOf[Organisaatio], None), json)) match {
        case o: OrganisaatioWithOid =>
          ContextualExtractor.getContext[{def organisaatioRepository: OrganisaatioRepository}] match {
            case Some(context) => context.organisaatioRepository.getOrganisaatio(o.oid) match {
              case Some(org) if (c.isInstance(org)) => org
              case Some(org) => ContextualExtractor.extractionError(KoskiErrorCategory.badRequest.validation.organisaatio.vääränTyyppinen("Organisaatio " + o.oid + " ei ole " + c.getSimpleName.toLowerCase + " vaan " + org.getClass.getSimpleName.toLowerCase))
              case None =>
                ContextualExtractor.extractionError(KoskiErrorCategory.badRequest.validation.organisaatio.tuntematon("Organisaatiota " + o.oid + " ei löydy organisaatiopalvelusta"))
            }
          }
        case org: Organisaatio => org
      }
  }
}
