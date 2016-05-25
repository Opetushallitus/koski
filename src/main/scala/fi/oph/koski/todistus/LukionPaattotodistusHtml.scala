package fi.oph.koski.todistus

import java.text.NumberFormat

import fi.oph.koski.localization.Locale
import fi.oph.koski.localization.Locale.finnish
import fi.oph.koski.schema._
import fi.oph.koski.koskiuser.{KoskiUser, KoskiUser$}


class LukionPaattotodistusHtml(implicit val user: KoskiUser) extends TodistusHtml {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: LukionOppimääränSuoritus) = {
    val oppiaineet: List[LukionOppiaineenSuoritus] = päättötodistus.osasuoritukset.toList.flatten
    val decimalFormat = NumberFormat.getInstance(finnish)

    def oppiaineenKurssimäärä(oppiaine: LukionOppiaineenSuoritus): Float = oppiaine.osasuoritukset.toList.flatten.foldLeft(0f) {
      (laajuus: Float, kurssi: LukionKurssinSuoritus) => laajuus + kurssi.koulutusmoduuli.laajuus.map(_.arvo).getOrElse(0f)
    }

    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/tor/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/tor/css/todistus-lukio.css"></link>
      </head>
      <body>
        <div class="todistus lukio">
          <h1>Lukion päättötodistus</h1>
          <h2 class="koulutustoimija">{i(koulutustoimija.flatMap(_.nimi))}</h2>
          <h2 class="oppilaitos">{i(oppilaitos.nimi)}</h2>
          <h3 class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">{oppijaHenkilö.hetu}</span>
          </h3>
          <div>on suorittanut lukion koko oppimäärän ja saanut tiedoistaan ja taidoistaan seuraavat arvosanat:</div>
          <table class="arvosanat">
            <tr>
              <th class="oppiaine">Oppiaineet</th>
              <th class="laajuus">Opiskeltujen kurssien määrä</th>
              <th class="arvosana-kirjaimin">Arvosana kirjaimin</th>
              <th class="arvosana-numeroin">Arvosana numeroin</th>
            </tr>
            {
              oppiaineet.map { oppiaine =>
                val nimiTeksti = i(oppiaine.koulutusmoduuli)
                val rowClass="oppiaine " + oppiaine.koulutusmoduuli.tunniste.koodiarvo
                <tr class={rowClass}>
                  <td class="oppiaine">{nimiTeksti}</td>
                  <td class="laajuus">{decimalFormat.format(oppiaineenKurssimäärä(oppiaine))}</td>
                  <td class="arvosana-kirjaimin">{i(oppiaine.arvosanaKirjaimin).capitalize}</td>
                  <td class="arvosana-numeroin">{oppiaine.arvosanaNumeroin}</td>
                </tr>
              }
            }
            <tr class="kurssimaara">
              <td class="kurssimaara-title">Opiskelijan suorittama kokonaiskurssimäärä</td>
              <td>{decimalFormat.format(oppiaineet.foldLeft(0f) { (summa, aine) => summa + oppiaineenKurssimäärä(aine)})}</td>
            </tr>
          </table>
          { päättötodistus.vahvistus.toList.map(vahvistusHTML)}
        </div>
      </body>
    </html>
  }
}
