package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.schema._

class PerusopetuksenLukuvuositodistusHtml(
                                           val koulutustoimija: Option[OrganisaatioWithOid],
                                           val oppilaitos: Oppilaitos,
                                           val oppijaHenkilö: Henkilötiedot,
                                           val todistus: PerusopetuksenVuosiluokanSuoritus)
                                         (implicit val user: KoskiSession, val localizationRepository: LocalizationRepository) extends PeruskoulunTodistusHtml[Suoritus] {
  def title = "Lukuvuositodistus - " + i(todistus.koulutusmoduuli.tunniste.nimi)
  def oppiaineet = todistus.osasuoritukset.toList.flatten

  override def oppijaHtml = <h3 class="oppija">
    <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
    <span class="hetu">{oppijaHenkilö.hetuStr}</span>
    <span class="luokka">{todistus.luokka}</span>
  </h3>

}
