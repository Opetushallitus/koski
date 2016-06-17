package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.schema._

class LuvaTodistusHtml(implicit val user: KoskiUser) extends TodistusHtml {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: Suoritus) = {
    val oppiaineet: List[Suoritus] = päättötodistus.osasuoritukset.toList.flatten

    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-lukio.css"></link>
      </head>
      <body>
        <div class="todistus lukio">
          <h1>Lukioon valmistavan koulutuksen päättötodistus</h1>
          <h2 class="koulutustoimija">{i(koulutustoimija.flatMap(_.nimi))}</h2>
          <h2 class="oppilaitos">{i(oppilaitos.nimi)}</h2>
          <h3 class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">{oppijaHenkilö.hetu}</span>
          </h3>
          <div>on suorittanut lukioon valmistavan koulutuksen koko oppimäärän ja saanut tiedoistaan ja taidoistaan seuraavat arvosanat:</div>
          <table class="arvosanat">
            <tr>
              <th class="oppiaine">Oppiaineet</th>
              <th class="laajuus">Opiskeltujen kurssien määrä</th>
              <th class="arvosana-kirjaimin">Arvosana kirjaimin</th>
              <th class="arvosana-numeroin">Arvosana numeroin</th>
            </tr>
            {
              def tyypinKuvaus(tyyppi: Koodistokoodiviite) = tyyppi.koodiarvo match {
                case "luvakurssi" => finnish("Lukioon valmistavat opinnot")
                case "lukionkurssi" => finnish("Valinnaisena suoritetut lukiokurssit")
                case _ => tyyppi.nimi.getOrElse(finnish(tyyppi.koodiarvo))
              }
              oppiaineet.groupBy(s => tyypinKuvaus(s.tyyppi)).toList.sortBy(_._1.get("fi")).map { case (tyyppi, suoritukset) =>
                val väliotsikko = <tr class="valiotsikko">
                  <td class="oppiaine">{i(tyyppi)}</td>
                  <td class="laajuus">{decimalFormat.format(suoritukset.map(laajuus).sum)}</td>
                </tr>

                List(väliotsikko) ++ suoritukset.map { oppiaine =>
                  val nimiTeksti = i(oppiaine.koulutusmoduuli)
                  <tr class="oppiaine">
                    <td class="oppiaine">{nimiTeksti}</td>
                    <td class="laajuus">{decimalFormat.format(laajuus(oppiaine))}</td>
                    <td class="arvosana-kirjaimin">{i(oppiaine.arvosanaKirjaimin).capitalize}</td>
                    <td class="arvosana-numeroin">{i(oppiaine.arvosanaNumeroin)}</td>
                  </tr>
                }
              }
            }
            <tr class="kurssimaara">
              <td class="kurssimaara-title">Opiskelijan suorittama kokonaiskurssimäärä</td>
              <td>{decimalFormat.format(oppiaineet.map(laajuus).sum)}</td>
            </tr>
          </table>
          { päättötodistus.vahvistus.toList.map(vahvistusHTML)}
        </div>
      </body>
    </html>
  }
}