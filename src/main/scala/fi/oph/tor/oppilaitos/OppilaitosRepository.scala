package fi.oph.tor.oppilaitos

import fi.oph.tor.organisaatio.OrganisaatioHierarkia
import fi.oph.tor.schema.Organisaatio
import fi.oph.tor.user.UserContext

class OppilaitosRepository {
  def oppilaitokset(implicit context: UserContext): List[Organisaatio] = {
    findOppilaitokset("")
  }

  def findOppilaitokset(query: String)(implicit context: UserContext): List[Organisaatio] = {
    context.organisaatioPuu
      .findOrganisaatiot(org => org.organisaatiotyypit.contains("OPPILAITOS") && org.nimi.toLowerCase.contains(query.toLowerCase))
      .map(toOppilaitos)
  }

  def findById(id: String)(implicit context: UserContext): Option[Organisaatio] = {
    context.organisaatioPuu.findById(id).map(toOppilaitos)
  }

  private def toOppilaitos(org: OrganisaatioHierarkia) = Organisaatio(org.oid, Some(org.nimi))
}