package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._

class PerusopetuksenOppiaineenOppimaaranTodistusHtml(val koulutustoimija: Option[OrganisaatioWithOid], val oppilaitos: Oppilaitos, val oppijaHenkilö: Henkilötiedot, val todistus: PerusopetuksenOppiaineenOppimääränSuoritus)(implicit val user: KoskiSession) extends PeruskoulunTodistusHtml[PerusopetuksenOppiaineenOppimääränSuoritus] {
  def title = "Todistus perusopetuksen oppiaineen oppimäärän suorittamisesta"
  def oppiaineet = List(todistus)
}
