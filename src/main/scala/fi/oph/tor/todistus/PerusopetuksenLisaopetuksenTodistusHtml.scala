package fi.oph.tor.todistus

import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser

class PerusopetuksenLisaopetuksenTodistusHtml(implicit val user: TorUser) extends PeruskoulunTodistusHtml {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, todistus: PerusopetuksenLisäopetuksenSuoritus) = {
    val oppiaineet: List[OppiaineenSuoritus] = todistus.osasuoritukset.toList.flatten
    renderTodistus(koulutustoimija, oppilaitos, oppijaHenkilö, todistus, oppiaineet, "Todistus lisäopetuksen suorittamisesta")
  }
}
