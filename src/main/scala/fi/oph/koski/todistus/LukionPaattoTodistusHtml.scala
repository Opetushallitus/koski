package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._

import scala.xml.NodeSeq


class LukionPaattoTodistusHtml(implicit val user: KoskiSession) extends TodistusHtml {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: Suoritus) = {
    val oppiaineet: List[Suoritus] = päättötodistus.osasuoritukset.toList.flatten

    def oppiaineenKurssimäärä(oppiaine: Suoritus): Float = oppiaine.osasuoritukset.toList.flatten.map(laajuus).sum

    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-lukio.css"></link>
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
              oppiaineet.flatMap {
                case oppiaine: LukionOppiaineenSuoritus =>
                  List(<tr class={"oppiaine " + oppiaine.koulutusmoduuli.tunniste.koodiarvo}>
                         <td class="oppiaine"> {i(oppiaine.koulutusmoduuli)}</td>
                         <td class="laajuus">{decimalFormat.format(oppiaineenKurssimäärä(oppiaine))}</td>
                         <td class="arvosana-kirjaimin">{i(oppiaine.arvosanaKirjaimin).capitalize}</td>
                         <td class="arvosana-numeroin">{i(oppiaine.arvosanaNumeroin)}</td>
                       </tr>)
                case aineryhmä: MuidenLukioOpintojenSuoritus =>
                  <tr class={"oppiaine " + aineryhmä.koulutusmoduuli.tunniste.koodiarvo}>
                    <td class="oppiaine">{i(aineryhmä.koulutusmoduuli)}</td>
                  </tr> :: aineryhmä.osasuoritukset.toList.flatten.map( kurssi =>
                    <tr class="kurssi">
                      <td class="kurssi">{i(kurssi.koulutusmoduuli)}</td>
                      <td class="laajuus">{decimalFormat.format(laajuus(kurssi))}</td>
                      <td class="arvosana-kirjaimin">{i(kurssi.arvosanaKirjaimin).capitalize}</td>
                      <td class="arvosana-numeroin">{i(kurssi.arvosanaNumeroin)}</td>
                    </tr>
                  )
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