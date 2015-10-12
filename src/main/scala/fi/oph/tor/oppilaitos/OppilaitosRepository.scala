package fi.oph.tor.oppilaitos

import fi.oph.tor.organisaatio.Organisaatio
import fi.oph.tor.user.UserContext

class OppilaitosRepository {
  def oppilaitokset(implicit context: UserContext): List[Oppilaitos] = {
    findOppilaitokset("")
  }

  def findOppilaitokset(query: String)(implicit context: UserContext): List[Oppilaitos] = {
    context.organisaatioPuu
      .findOrganisaatiot(org => org.organisaatiotyypit.contains("OPPILAITOS") && org.nimi.toLowerCase.contains(query.toLowerCase))
      .map(toOppilaitos)
  }

  def findById(id: String)(implicit context: UserContext): Option[Oppilaitos] = {
    context.organisaatioPuu.findById(id).map(toOppilaitos)
  }

  private def toOppilaitos(org: Organisaatio) = Oppilaitos(org.oid, org.nimi)
}