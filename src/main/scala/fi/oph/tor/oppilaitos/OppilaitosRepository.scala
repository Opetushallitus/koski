package fi.oph.tor.oppilaitos

import fi.oph.tor.organisaatio.OrganisaatioHierarkia
import fi.oph.tor.schema.Organisaatio
import fi.oph.tor.toruser.TorUser

class OppilaitosRepository {
  def oppilaitokset(implicit context: TorUser): Iterable[Organisaatio] = {
    findOppilaitokset("")
  }

  def findOppilaitokset(query: String)(implicit context: TorUser): Iterable[Organisaatio] = {
    context.userOrganisations
      .findOrganisaatiot(org => org.organisaatiotyypit.contains("OPPILAITOS") && org.nimi.toLowerCase.contains(query.toLowerCase))
      .map(toOppilaitos)
      .toList
  }

  def findById(id: String)(implicit context: TorUser): Option[Organisaatio] = {
    context.userOrganisations.getOrganisaatio(id).map(toOppilaitos)
  }

  private def toOppilaitos(org: OrganisaatioHierarkia) = Organisaatio(org.oid, Some(org.nimi))
}