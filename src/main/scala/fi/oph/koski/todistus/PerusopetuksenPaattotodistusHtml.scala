package fi.oph.koski.todistus

import fi.oph.koski.schema._
import fi.oph.koski.koskiuser.KoskiUser

class PerusopetuksenPaattotodistusHtml(val koulutustoimija: Option[OrganisaatioWithOid], val oppilaitos: Oppilaitos, val oppijaHenkilö: Henkilötiedot, val todistus: PerusopetuksenOppimääränSuoritus)(implicit val user: KoskiUser) extends PeruskoulunTodistusHtml[OppiaineenTaiToimintaAlueenSuoritus] {
  def title = "Perusopetuksen päättötodistus"
  def oppiaineet = todistus.osasuoritukset.toList.flatten
}
