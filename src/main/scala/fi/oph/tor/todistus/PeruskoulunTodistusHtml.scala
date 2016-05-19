package fi.oph.tor.todistus

import java.time.format.DateTimeFormatter

import fi.oph.tor.schema._

import scala.xml.Elem

trait PeruskoulunTodistusHtml extends TodistusHtml {
  def renderTodistus(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: PerusopetuksenPäätasonSuoritus, oppiaineet: List[OppiaineenSuoritus], title: String): Elem = {
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
      val nimiTeksti = i(oppiaine.suoritus.koulutusmoduuli)
      val nimi = oppiaine match {
        case LiittyväValinnainen(suoritus) => "Valinnainen " + decapitalize(nimiTeksti)
        case _ => nimiTeksti
      }
      val rowClass = "oppiaine " + oppiaine.suoritus.koulutusmoduuli.tunniste.koodiarvo + (if (oppiaine.suoritus.koulutusmoduuli.pakollinen) {
        ""
      } else {
        " valinnainen"
      })
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
          {oppiaine.suoritus.arvosanaNumeroin}
        </td>
      </tr>
    }

    val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")

    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/tor/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/tor/css/todistus-perusopetus.css"></link>
      </head>
      <body>
        <div class="todistus perusopetus">
          <h2 class="koulutustoimija">
            {i(koulutustoimija.flatMap(_.nimi))}
          </h2>
          <h1>{title}</h1>
          <h2 class="oppilaitos">
            {i(oppilaitos.nimi)}
          </h2>
          <h3 class="oppija">
            <span class="nimi">
              {oppijaHenkilö.sukunimi}
              ,
              {oppijaHenkilö.etunimet}
            </span>
            <span class="hetu">
              {oppijaHenkilö.hetu}
            </span>
          </h3>
          <table class="arvosanat">
            <tr>
              <th class="oppiaine">Yhteiset ja niihin liittyvät valinnaiset oppiaineet</th>
              <th class="laajuus">Vuosiviikko- tuntimäärä</th>
              <th class="arvosana">Arvosana</th>
            </tr>{arvosanaLista(pakollisetJaNiihinLiittyvätValinnaiset)}<tr>
            <th>Muut valinnaiset opinnot</th>
          </tr>{arvosanaLista(muutValinnaiset)}
          </table>{päättötodistus.vahvistus.toList.map(vahvistusHTML)}
        </div>
      </body>
    </html>
  }

  sealed trait Aine { def suoritus: OppiaineenSuoritus }
  case class Pakollinen(suoritus: OppiaineenSuoritus) extends Aine
  case class Valinnainen(suoritus: OppiaineenSuoritus) extends Aine
  case class LiittyväValinnainen(suoritus: OppiaineenSuoritus) extends Aine
}
