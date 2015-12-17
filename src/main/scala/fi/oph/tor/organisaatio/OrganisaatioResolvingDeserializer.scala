package fi.oph.tor.organisaatio

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.{ContextualExtractor, Json}
import fi.oph.tor.schema.{OidOrganisaatio, OrganisaatioDeserializer, Deserializer, Organisaatio}
import fi.vm.sade.utils.slf4j.Logging
import org.json4s._
import org.json4s.reflect.TypeInfo

object OrganisaatioResolvingDeserializer extends Deserializer[Organisaatio] with Logging {
  private val TheClass = classOf[Organisaatio]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Organisaatio] = {
    case (t, json) if OrganisaatioDeserializer.deserialize(Json.jsonFormats).isDefinedAt((t, json)) =>
      OrganisaatioDeserializer.deserialize(Json.jsonFormats)((t, json)) match {
        case OidOrganisaatio(oid, _) =>
          ContextualExtractor.getContext[{def organisaatioRepository: OrganisaatioRepository}] match {
            case Some(context) => context.organisaatioRepository.getOrganisaatio(oid) match {
              case Some(org) => OidOrganisaatio(org.oid, Some(org.nimi))
              case None => ContextualExtractor.extractionError(HttpStatus.badRequest("Organisaatiota " + oid + " ei löydy organisaatiopalvelusta"))
            }
          }
        case org => org
      }
  }
}
