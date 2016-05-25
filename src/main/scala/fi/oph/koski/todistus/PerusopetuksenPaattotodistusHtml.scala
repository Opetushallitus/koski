package fi.oph.koski.todistus

import fi.oph.koski.schema._
import fi.oph.koski.koskiuser.KoskiUser

class PerusopetuksenPaattotodistusHtml(implicit val user: KoskiUser) extends PeruskoulunTodistusHtml[PerusopetuksenOppiaineenSuoritus] {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: PerusopetuksenOppimääränSuoritus) = {
    renderTodistus(koulutustoimija, oppilaitos, oppijaHenkilö, päättötodistus, päättötodistus.osasuoritukset.toList.flatten, "Perusopetuksen päättötodistus")
  }
}
