package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.schema._

class PerusopetuksenLukuvuositodistusHtml(implicit val user: KoskiUser) extends PeruskoulunTodistusHtml[OppiaineenTaiToimintaAlueenSuoritus] {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: PerusopetuksenVuosiluokanSuoritus) = {
    renderTodistus(koulutustoimija, oppilaitos, oppijaHenkilö, päättötodistus, päättötodistus.osasuoritukset.toList.flatten, "Lukuvuositodistus")
  }
}
