package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.schema._

class PerusopetuksenLukuvuositodistusHtml(
                                           val koulutustoimija: Option[OrganisaatioWithOid],
                                           val oppilaitos: Oppilaitos,
                                           val oppijaHenkilö: Henkilötiedot,
                                           val todistus: PerusopetuksenVuosiluokanSuoritus)
                                         (implicit val user: KoskiUser) extends PeruskoulunTodistusHtml[OppiaineenTaiToimintaAlueenSuoritus] {
  def title = "Lukuvuositodistus"
  def oppiaineet = todistus.osasuoritukset.toList.flatten
}
