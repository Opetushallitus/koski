package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.schema._

class ValmaTodistusHtml(val koulutustoimija: Option[OrganisaatioWithOid], val oppilaitos: Oppilaitos, val oppijaHenkilö: Henkilötiedot, val todistus: Suoritus)(implicit val user: KoskiUser) extends ValmentavanKoulutuksenTodistusHtml {
  def title = "Ammatilliseen peruskoulutukseen valmentava koulutus"
}
