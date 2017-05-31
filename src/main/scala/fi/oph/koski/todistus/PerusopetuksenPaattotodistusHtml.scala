package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.schema._

class PerusopetuksenPaattotodistusHtml(val koulutustoimija: Option[OrganisaatioWithOid], val oppilaitos: Oppilaitos, val oppijaHenkilö: Henkilötiedot, val todistus: PerusopetuksenOppimääränSuoritus)(implicit val user: KoskiSession, val localizationRepository: LocalizationRepository) extends PeruskoulunTodistusHtml[OppiaineenTaiToiminta_AlueenSuoritus] {
  def title = "Perusopetuksen päättötodistus"
  def oppiaineet = todistus.osasuoritukset.toList.flatten
}
