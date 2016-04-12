package fi.oph.tor.todistus

import java.time.format.DateTimeFormatter
import fi.oph.tor.schema._

object PeruskoulunPaattotodistusHtml {
  def renderPeruskoulunPäättötodistus(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: PeruskoulunPäättötodistus) = {
    val oppiaineet: List[PeruskoulunOppiaineenSuoritus] = päättötodistus.osasuoritukset.toList.flatten
    val pakolliset = oppiaineet.filter(_.koulutusmoduuli.pakollinen)
    val pakollisetJaNiihinLiittyvätValinnaiset: List[Aine] = pakolliset.flatMap { case pakollinen =>
      val liittyvätValinnaiset: List[LiittyväValinnainen] = oppiaineet
        .filter(aine => !aine.koulutusmoduuli.pakollinen && aine.koulutusmoduuli.tunniste == pakollinen.koulutusmoduuli.tunniste)
        .map(LiittyväValinnainen(_))
      Pakollinen(pakollinen) :: liittyvätValinnaiset
    }
    val muutValinnaiset = oppiaineet.filter(!pakollisetJaNiihinLiittyvätValinnaiset.map(_.suoritus).contains(_))
      .map(Valinnainen(_))

    def arvosanaLista(oppiaineet: List[Aine]) = oppiaineet.map { oppiaine =>
      val nimiTeksti = oppiaine.suoritus.koulutusmoduuli.toString
      val nimi = oppiaine match {
        case LiittyväValinnainen(suoritus) => "Valinnainen " + nimiTeksti
        case _ => nimiTeksti
      }
      val rowClass="oppiaine " + oppiaine.suoritus.koulutusmoduuli.tunniste.koodiarvo
      <tr class={rowClass}>
        <td class="oppiaine">{nimi}</td>
        <td class="laajuus">{oppiaine.suoritus.koulutusmoduuli.laajuus.map(_.arvo).getOrElse("")}</td>
        <td class="arvosana-kirjaimin">{oppiaine.suoritus.arviointi.toList.flatten.lastOption.map(_.arvosanaKirjaimin("fi")).getOrElse("").capitalize}</td>
        <td class="arvosana-numeroin">{oppiaine.suoritus.arviointi.toList.flatten.lastOption.flatMap(_.arvosanaNumeroin).getOrElse("")}</td>
      </tr>
    }

    val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")

    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/tor/css/todistus-peruskoulu.css"></link>
      </head>
      <body>
        <div class="todistus peruskoulu">
          <h2 class="koulutustoimija">{koulutustoimija.flatMap(_.nimi).getOrElse("")}</h2>
          <h1>Peruskoulun päättötodistus</h1>
          <h2 class="oppilaitos">{oppilaitos.nimi.getOrElse("")}</h2>
          <div class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">{oppijaHenkilö.hetu}</span>
          </div>
          <table class="arvosanat">
            <tr>
              <th class="oppiaine">Yhteiset ja niihin liittyvät valinnaiset oppiaineet</th>
              <th class="laajuus">Vuosiviikko- tuntimäärä</th>
              <th class="arvosana">Arvosana</th>
            </tr>
            { arvosanaLista(pakollisetJaNiihinLiittyvätValinnaiset) }
            <tr>
              <th>Muut valinnaiset opinnot</th>
            </tr>
            { arvosanaLista(muutValinnaiset) }
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

  sealed trait Aine { def suoritus: PeruskoulunOppiaineenSuoritus }
  case class Pakollinen(suoritus: PeruskoulunOppiaineenSuoritus) extends Aine
  case class Valinnainen(suoritus: PeruskoulunOppiaineenSuoritus) extends Aine
  case class LiittyväValinnainen(suoritus: PeruskoulunOppiaineenSuoritus) extends Aine
}
