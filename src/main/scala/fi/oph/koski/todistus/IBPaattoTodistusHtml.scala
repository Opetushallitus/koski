package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.schema._


class IBPaattoTodistusHtml(implicit val user: KoskiUser) extends TodistusHtml {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: Suoritus) = {
    val oppiaineet: List[Suoritus] = päättötodistus.osasuoritukset.toList.flatten

    def oppiaineenKurssimäärä(oppiaine: Suoritus): Float = oppiaine.osasuoritukset.toList.flatten.map(laajuus).sum

    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
      </head>
      <body>
        <div class="todistus lukio">
          <h1>International Baccalaureate</h1>
          <h1>Predicted Grades</h1>
          <h2 class="koulutustoimija">{i(koulutustoimija.flatMap(_.nimi))}</h2>
          <h2 class="oppilaitos">{i(oppilaitos.nimi)}</h2>
          <h3 class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">{oppijaHenkilö.hetu}</span>
          </h3>
          <table class="arvosanat">
            <tr>
              <th class="oppiaine">Subject</th>
              <th class="laajuus">Level</th>
              <th class="arvosana-kirjaimin">Grades in words</th>
              <th class="arvosana-numeroin">Grades in numbers</th>
            </tr>
            {
              oppiaineet.map { oppiaine =>
                val nimiTeksti = i(oppiaine.koulutusmoduuli)
                val rowClass="oppiaine " + oppiaine.koulutusmoduuli.tunniste.koodiarvo
                <tr class={rowClass}>
                  <td class="oppiaine">{nimiTeksti}</td>
                  <td class="laajuus">{decimalFormat.format(oppiaineenKurssimäärä(oppiaine))}</td>
                  <td class="arvosana-kirjaimin">{i(oppiaine.arvosanaKirjaimin).capitalize}</td>
                  <td class="arvosana-numeroin">{i(oppiaine.arvosanaNumeroin)}</td>
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