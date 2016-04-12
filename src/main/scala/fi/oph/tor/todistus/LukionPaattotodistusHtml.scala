package fi.oph.tor.todistus

import java.time.format.DateTimeFormatter

import fi.oph.tor.schema._

object LukionPaattotodistusHtml {
  def renderLukionPäättötodistus(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: LukionOppimääränSuoritus) = {
    val oppiaineet: List[LukionOppiaineenSuoritus] = päättötodistus.osasuoritukset.toList.flatten

    def arvosanaLista(oppiaineet: List[LukionOppiaineenSuoritus]) = oppiaineet.map { oppiaine =>
      val nimiTeksti = oppiaine.koulutusmoduuli.toString
      val rowClass="oppiaine " + oppiaine.koulutusmoduuli.tunniste.koodiarvo
      <tr class={rowClass}>
        <td class="oppiaine">{nimiTeksti}</td>
        <td class="laajuus">{oppiaine.koulutusmoduuli.laajuus.map(_.arvo).getOrElse("")}</td>
        <td class="arvosana-kirjaimin">{oppiaine.arviointi.toList.flatten.lastOption.map(_.arvosanaKirjaimin("fi")).getOrElse("")}</td>
        <td class="arvosana-numeroin">{oppiaine.arviointi.toList.flatten.lastOption.flatMap(_.arvosanaNumeroin).getOrElse("")}</td>
      </tr>
    }

    val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")

    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/tor/css/todistus-lukio.css"></link>
      </head>
      <body>
        <div class="todistus lukio">
          <h1>Lukion päättötodistus</h1>
          <h2 class="koulutustoimija">{koulutustoimija.flatMap(_.nimi).getOrElse("")}</h2>
          <h2 class="oppilaitos">{oppilaitos.nimi.getOrElse("")}</h2>
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
            { arvosanaLista(oppiaineet) }
          </table>
          <div class="vahvistus">
            <span class="paikkakunta">Tampere<!-- TODO: paikkakuntaa ei ole datassa --></span>
            <span class="date">{päättötodistus.vahvistus.map(_.päivä).map(dateFormatter.format(_)).getOrElse("")}</span>
            {
            päättötodistus.vahvistus.flatMap(_.myöntäjäHenkilöt).toList.flatten.map { myöntäjäHenkilö =>
              <span class="allekirjoitus">
                <div class="viiva">&nbsp;</div>
                <div class="nimenselvennys">{myöntäjäHenkilö.nimi}</div>
                <div class="titteli">{myöntäjäHenkilö.titteli}</div>
              </span>
            }
            }
          </div>
        </div>
      </body>
    </html>
  }
}
