package fi.oph.koski.todistus

import fi.oph.koski.schema._
import fi.oph.koski.koskiuser.KoskiUser

class PerusopetuksenOppiaineenOppimaaranTodistusHtml(implicit val user: KoskiUser) extends PeruskoulunTodistusHtml[PerusopetuksenOppiaineenOppimääränSuoritus] {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, oppiaineenSuoritus: PerusopetuksenOppiaineenOppimääränSuoritus) = {
    val oppiaineet = List(oppiaineenSuoritus)
    renderTodistus(koulutustoimija, oppilaitos, oppijaHenkilö, oppiaineenSuoritus, oppiaineet, "Perusopetuksen oppiaineen oppimäärän suoritus")
  }
}
