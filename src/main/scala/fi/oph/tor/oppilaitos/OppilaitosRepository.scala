package fi.oph.tor.oppilaitos

import fi.oph.tor.organisaatio.OrganisaatioHierarkia
import fi.oph.tor.schema.Organisaatio
import fi.oph.tor.user.UserContext

class OppilaitosRepository {
  def oppilaitokset(implicit context: UserContext): Iterable[Organisaatio] = {
    findOppilaitokset("")
  }

  def findOppilaitokset(query: String)(implicit context: UserContext): Iterable[Organisaatio] = {
    context.userOrganisations
      .findOrganisaatiot(org => org.organisaatiotyypit.contains("OPPILAITOS") && org.nimi.toLowerCase.contains(query.toLowerCase))
      .map(toOppilaitos)
      .toList
  }

  def findById(id: String)(implicit context: UserContext): Option[Organisaatio] = {
    context.userOrganisations.findById(id).map(toOppilaitos)
  }

  private def toOppilaitos(org: OrganisaatioHierarkia) = Organisaatio(org.oid, Some(org.nimi))
}