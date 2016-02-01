package fi.oph.tor.organisaatio

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.http.TorErrorCode.Validation
import fi.oph.tor.json.{ContextualExtractor, Json}
import fi.oph.tor.schema._
import fi.oph.tor.log.Logging
import org.json4s._
import org.json4s.reflect.TypeInfo

object OrganisaatioResolvingDeserializer extends Deserializer[Organisaatio] with Logging {
  val OppilaitosClass = classOf[Oppilaitos]
  val organisaatioClasses = List(classOf[Organisaatio], classOf[OrganisaatioWithOid], classOf[Oppilaitos], classOf[OidOrganisaatio], classOf[Tutkintotoimikunta], classOf[Yritys])

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Organisaatio] = {
    case (TypeInfo(c, _), json) if organisaatioClasses.contains(c) =>
      OrganisaatioDeserializer.deserialize(Json.jsonFormats)((TypeInfo(classOf[Organisaatio], None), json)) match {
        case o: OrganisaatioWithOid =>
          ContextualExtractor.getContext[{def organisaatioRepository: OrganisaatioRepository}] match {
            case Some(context) => context.organisaatioRepository.getOrganisaatio(o.oid) match {
              case Some(org) if (c.isInstance(org)) => org
              case Some(org) => ContextualExtractor.extractionError(HttpStatus.badRequest(Validation.organisaatioVääränTyyppinen, "Organisaatio " + o.oid + " ei ole " + c.getSimpleName))
              case None => ContextualExtractor.extractionError(HttpStatus.badRequest(Validation.organisaatioTuntematon, "Organisaatiota " + o.oid + " ei löydy organisaatiopalvelusta"))
            }
          }
        case org => org
      }
  }
}
