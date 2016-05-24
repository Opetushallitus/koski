package fi.oph.tor.todistus

import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser

class PerusopetuksenPaattotodistusHtml(implicit val user: TorUser) extends PeruskoulunTodistusHtml[PerusopetuksenOppiaineenSuoritus] {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: PerusopetuksenOppimääränSuoritus) = {
    renderTodistus(koulutustoimija, oppilaitos, oppijaHenkilö, päättötodistus, päättötodistus.osasuoritukset.toList.flatten, "Perusopetuksen päättötodistus")
  }
}
