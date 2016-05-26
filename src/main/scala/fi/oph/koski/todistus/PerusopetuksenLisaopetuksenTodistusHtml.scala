package fi.oph.koski.todistus

import fi.oph.koski.schema._
import fi.oph.koski.koskiuser.KoskiUser

import scala.xml.Elem

class PerusopetuksenLisaopetuksenTodistusHtml(implicit val user: KoskiUser) extends PeruskoulunTodistusHtml[PerusopetuksenLisäopetuksenOppiaineenSuoritus] {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, todistus: PerusopetuksenLisäopetuksenSuoritus) = {
    val oppiaineet = todistus.osasuoritukset.toList.flatten
    renderTodistus(koulutustoimija, oppilaitos, oppijaHenkilö, todistus, oppiaineet, "Todistus lisäopetuksen suorittamisesta")
  }

  override def renderHeader: Elem =
    <tr>
      <th class="oppiaine">Yhteiset ja niihin liittyvät valinnaiset oppiaineet</th>
      <th class="laajuus">Vuosiviikko- tuntimäärä</th>
      <th class="arvosana">Arvosana</th>
      <th></th>
      <th class="arvosana-korotus">Perusopetuksen päättöarvosanojen korottaminen</th>
    </tr>

  override def renderRows(oppiaine: Aine, nimi: String, rowClass: String): Elem = {
    val korotus = oppiaine.suoritus.arviointi.exists(_.lastOption.exists(_.korotus))
    <tr class={rowClass}>
      <td class="oppiaine">
        {nimi}
      </td>
      <td class="laajuus">
        {oppiaine.suoritus.koulutusmoduuli.laajuus.map(_.arvo).getOrElse("")}
      </td>
      <td class="arvosana-kirjaimin">
        {i(oppiaine.suoritus.arvosanaKirjaimin).capitalize}
      </td>
      <td class="arvosana-numeroin">
        {if (!korotus) i(oppiaine.suoritus.arvosanaNumeroin)}
      </td>
      <td class="arvosana-korotus">
        {if (korotus) i(oppiaine.suoritus.arvosanaNumeroin)}
      </td>
    </tr>

  }
}
