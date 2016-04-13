package fi.oph.tor.oppilaitos

import fi.oph.tor.organisaatio.{OrganisaatioRepository, OrganisaatioHierarkia}
import fi.oph.tor.schema.OidOrganisaatio
import fi.oph.tor.toruser.TorUser

class OppilaitosRepository(organisatioRepository: OrganisaatioRepository) {
  def oppilaitokset(implicit context: TorUser): Iterable[OidOrganisaatio] = {
    findOppilaitokset("")
  }

  def findOppilaitokset(query: String)(implicit context: TorUser): Iterable[OidOrganisaatio] = {
    context.organisationOids
      .flatMap(oid => organisatioRepository.getOrganisaatioHierarkia(oid))
      .filter(org => org.organisaatiotyypit.contains("OPPILAITOS") && org.nimi.get("fi").toLowerCase.contains(query.toLowerCase))
      .map(toOppilaitos)
      .toList
  }

  private def toOppilaitos(org: OrganisaatioHierarkia) = OidOrganisaatio(org.oid, Some(org.nimi))
}