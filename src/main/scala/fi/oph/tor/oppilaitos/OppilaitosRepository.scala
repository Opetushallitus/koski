package fi.oph.tor.oppilaitos

import fi.oph.tor.organisaatio.OrganisaatioHierarkia
import fi.oph.tor.schema.OidOrganisaatio
import fi.oph.tor.toruser.TorUser

class OppilaitosRepository {
  def oppilaitokset(implicit context: TorUser): Iterable[OidOrganisaatio] = {
    findOppilaitokset("")
  }

  def findOppilaitokset(query: String)(implicit context: TorUser): Iterable[OidOrganisaatio] = {
    context.userOrganisations
      .findOrganisaatiot(org => org.organisaatiotyypit.contains("OPPILAITOS") && org.nimi.toLowerCase.contains(query.toLowerCase))
      .map(toOppilaitos)
      .toList
  }

  def findById(id: String)(implicit context: TorUser): Option[OidOrganisaatio] = {
    context.userOrganisations.getOrganisaatioHierarkia(id).map(toOppilaitos)
  }

  private def toOppilaitos(org: OrganisaatioHierarkia) = OidOrganisaatio(org.oid, Some(org.nimi))
}