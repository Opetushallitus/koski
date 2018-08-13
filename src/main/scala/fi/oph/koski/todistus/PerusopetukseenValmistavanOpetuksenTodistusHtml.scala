package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.schema._

class PerusopetukseenValmistavanOpetuksenTodistusHtml(val koulutustoimija: Option[OrganisaatioWithOid], val oppilaitos: Oppilaitos, val oppijaHenkilö: Henkilötiedot, val todistus: PerusopetukseenValmistavanOpetuksenSuoritus)(implicit val user: KoskiSession, val localizationRepository: LocalizationRepository) extends TodistusHtml {
  def todistusHtml = <html lang={lang}>
    <head>
      <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
      <link rel="stylesheet" type="text/css" href="/koski/css/todistus-perusopetus.css"></link>
    </head>
    <body>
      <div class="todistus perusopetus-valmistava">
        <h1>Todistus perusopetukseen valmistavaan opetukseen osallistumisesta</h1>
        <h2 class="koulutustoimija">{i(koulutustoimija.flatMap(_.nimi))}</h2>
        <h2 class="oppilaitos">{i(oppilaitos.nimi)}</h2>
        <h3 class="oppija">
          <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
          <span class="hetu">{oppijaHenkilö.hetuStr}</span>
        </h3>
        <table class="arvosanat">
          <tr>
            <th class="oppiaine">Oppiaineet ja oppiaineen sisältö</th>
            <th class="laajuus">Laajuus vuosiviikkotunneissa</th>
            <th class="arvosana-kirjaimin">Arvosana</th>
          </tr>
          {
            todistus.osasuoritukset.toList.flatten.map { oppiaine =>
              <tr class="oppiaine">
                <td class="oppiaine">
                  <span class="nimi">{i(oppiaine.koulutusmoduuli.nimi)}</span>
                  <span class="kuvaus">{opetuksenSisältö(oppiaine)}</span>
                </td>
                <td class="laajuus">{decimalFormat.format(laajuus(oppiaine))}</td>
                <td class="arvosana">{i(oppiaine.sanallinenArviointi)}</td>
              </tr>
            }
          }
        </table>
        { todistus.vahvistus.toList.map(vahvistusHTML)}
      </div>
    </body>
  </html>

  private def opetuksenSisältö(oppiaineenSuoritus: PerusopetukseenValmistavanOpetuksenOsasuoritus) = i(oppiaineenSuoritus match {
    case v: PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus => v.koulutusmoduuli.opetuksenSisältö
    case _ => None
  })
}
