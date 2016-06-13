package fi.oph.koski.todistus

import java.time.format.DateTimeFormatter

import fi.oph.koski.schema._

import scala.xml.Elem

trait PeruskoulunTodistusHtml[T <: Suoritus] extends TodistusHtml {
  def renderTodistus(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: Suoritus, oppiaineet: List[T], title: String): Elem = {
    def onPakollinen(suoritus: Suoritus) = suoritus match {
      case s:OppiaineenSuoritus => s.koulutusmoduuli.pakollinen
      case _ => true
    }
    val pakolliset = oppiaineet.filter(onPakollinen)
    val pakollisetJaNiihinLiittyvätValinnaiset: List[Aine] = pakolliset.flatMap { case pakollinen =>
      val liittyvätValinnaiset: List[LiittyväValinnainen] = oppiaineet
        .filter(aine => !onPakollinen(aine) && aine.koulutusmoduuli.tunniste == pakollinen.koulutusmoduuli.tunniste)
        .map(LiittyväValinnainen)
      Pakollinen(pakollinen) :: liittyvätValinnaiset
    }
    val muutValinnaiset = oppiaineet.filter(!pakollisetJaNiihinLiittyvätValinnaiset.map(_.suoritus).contains(_))
      .map(Valinnainen)

    def arvosanaLista(oppiaineet: List[Aine]) = renderHeader :: oppiaineet.map { oppiaine =>
      val nimiTeksti = i(oppiaine.suoritus.koulutusmoduuli)
      val nimi = oppiaine match {
        case LiittyväValinnainen(suoritus) => "Valinnainen " + decapitalize(nimiTeksti)
        case _ => nimiTeksti
      }
      val rowClass = "oppiaine " + oppiaine.suoritus.koulutusmoduuli.tunniste.koodiarvo + (if (onPakollinen(oppiaine.suoritus)) {
        ""
      } else {
        " valinnainen"
      })
      renderRows(oppiaine, nimi, rowClass)
    }

    def muutOpinnot(valinnaiset: List[Valinnainen]) =
      if (valinnaiset.nonEmpty)
          <th class="muut-opinnot">Muut valinnaiset opinnot</th> ++
          {arvosanaLista(valinnaiset)}
      else Nil

    val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")

    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-perusopetus.css"></link>
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
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">{oppijaHenkilö.hetu}</span>
          </h3>
          <table class="arvosanat">
            {arvosanaLista(pakollisetJaNiihinLiittyvätValinnaiset)}
            {muutOpinnot(muutValinnaiset)}
          </table>
          {päättötodistus.vahvistus.toList.map(vahvistusHTML)}
        </div>
      </body>
    </html>
  }

  def renderHeader: Elem =
    <tr>
      <th class="oppiaine">Yhteiset ja niihin liittyvät valinnaiset oppiaineet</th>
      <th class="laajuus">Vuosiviikko- tuntimäärä</th>
      <th class="arvosana">Arvosana</th>
    </tr>

  def renderRows(oppiaine: Aine, nimi: String, rowClass: String): Elem = {
    <tr class={rowClass}>
      <td class="oppiaine">
        {nimi}
      </td>
      <td class="laajuus">
        {oppiaine.suoritus.koulutusmoduuli.laajuus.map(_.arvo).getOrElse("")}
      </td>
      <td class="arvosana-kirjaimin">
        {
          oppiaine.suoritus.sanallinenArviointi match {
            case Some(sanallinenArviointi) => <span class="sanallinen">{i(sanallinenArviointi)}</span>
            case None => i(oppiaine.suoritus.arvosanaKirjaimin).capitalize
          }
        }
      </td>
      <td class="arvosana-numeroin">
        {i(oppiaine.suoritus.arvosanaNumeroin)}
      </td>
    </tr>
  }

  sealed trait Aine { def suoritus: T }
  case class Pakollinen(suoritus: T) extends Aine
  case class Valinnainen(suoritus: T) extends Aine
  case class LiittyväValinnainen(suoritus: T) extends Aine
}
