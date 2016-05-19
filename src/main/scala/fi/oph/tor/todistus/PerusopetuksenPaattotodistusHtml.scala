package fi.oph.tor.todistus

import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser

class PerusopetuksenPaattotodistusHtml(implicit val user: TorUser) extends PeruskoulunTodistusHtml {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: PerusopetuksenOppimääränSuoritus) = {
    val oppiaineet: List[PerusopetuksenOppiaineenSuoritus] = päättötodistus.osasuoritukset.toList.flatten
    renderTodistus(koulutustoimija, oppilaitos, oppijaHenkilö, päättötodistus, oppiaineet, "Perusopetuksen päättötodistus")
  }
}
