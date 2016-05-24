package fi.oph.tor.todistus

import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser

class PerusopetuksenOppiaineenOppimaaranTodistusHtml(implicit val user: TorUser) extends PeruskoulunTodistusHtml[PerusopetuksenOppiaineenOppimääränSuoritus] {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, oppiaineenSuoritus: PerusopetuksenOppiaineenOppimääränSuoritus) = {
    val oppiaineet = List(oppiaineenSuoritus)
    renderTodistus(koulutustoimija, oppilaitos, oppijaHenkilö, oppiaineenSuoritus, oppiaineet, "Perusopetuksen oppiaineen oppimäärän suoritus")
  }
}
