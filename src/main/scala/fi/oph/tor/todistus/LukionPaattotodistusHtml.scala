package fi.oph.tor.todistus

import java.text.DecimalFormat

import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser

class LukionPaattotodistusHtml(implicit val user: TorUser) extends TodistusHtml {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: LukionOppimääränSuoritus) = {
    val oppiaineet: List[LukionOppiaineenSuoritus] = päättötodistus.osasuoritukset.toList.flatten
    val decimalFormat = new DecimalFormat("#.#")

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
          <div class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">{oppijaHenkilö.hetu}</span>
          </div>
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
