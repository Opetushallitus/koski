package fi.oph.koski.todistus

import fi.oph.koski.schema._

import scala.xml.Elem

trait PeruskoulunTodistusHtml[T <: Suoritus] extends TodistusHtml {
  def koulutustoimija: Option[OrganisaatioWithOid]
  def oppilaitos: Oppilaitos
  def oppijaHenkilö: Henkilötiedot
  def todistus: Suoritus
  def oppiaineet: List[T]
  def title: String

  private def onPakollinen(suoritus: Suoritus) = suoritus.koulutusmoduuli match {
    case s: Valinnaisuus => s.pakollinen
    case _ => true
  }
  def onYksilöllistetty(suoritus: Suoritus) = suoritus match {
    case s: Yksilöllistettävä => s.yksilöllistettyOppimäärä
    case _ => false
  }

  private val pakolliset = oppiaineet.filter(onPakollinen)
  private val pakollisetJaNiihinLiittyvätValinnaiset: List[Aine] = pakolliset.flatMap { case pakollinen =>
    val liittyvätValinnaiset: List[LiittyväValinnainen] = oppiaineet
      .filter(aine => !onPakollinen(aine) && aine.koulutusmoduuli.tunniste == pakollinen.koulutusmoduuli.tunniste)
      .map(LiittyväValinnainen)
    Pakollinen(pakollinen) :: liittyvätValinnaiset
  }
  private val muutValinnaiset = oppiaineet.filter(!pakollisetJaNiihinLiittyvätValinnaiset.map(_.suoritus).contains(_))
    .map(Valinnainen)

  private def arvosanaLista(oppiaineet: List[Aine]) = oppiaineetHeaderHtml :: oppiaineet.map { oppiaine =>
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
    oppiainerivitHtml(oppiaine, nimi, rowClass)
  }

  private def muutOpinnot(valinnaiset: List[Valinnainen]) =
    if (valinnaiset.nonEmpty)
      <th class="muut-opinnot">Muut valinnaiset opinnot</th> ++
        {arvosanaLista(valinnaiset)}
    else Nil

  def todistusHtml = <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-perusopetus.css"></link>
      </head>
      <body>
        <div class="todistus perusopetus">
          {otsikkoHtml}
          {oppijaHtml}
          <table class="arvosanat">
            {arvosanaLista(pakollisetJaNiihinLiittyvätValinnaiset)}
            {muutOpinnot(muutValinnaiset)}
          </table>
          {
            if (oppiaineet.exists(onYksilöllistetty)) {
              <div class="sisaltaa-yksilollistettyja">
                <p>Oppilas on opiskellut tähdellä (*) merkityt oppiaineet yksilöllistetyn oppimäärän mukaan.</p>
              </div>
            }
          }
          {todistus.vahvistus.toList.map(vahvistusHTML)}
        </div>
      </body>
    </html>

  def otsikkoHtml = <div>
    <h2 class="koulutustoimija">
      {i(koulutustoimija.flatMap(_.nimi))}
    </h2>
    <h1>{title}</h1>
    <h2 class="oppilaitos">
      {i(oppilaitos.nimi)}
    </h2>
  </div>

  def oppijaHtml = <h3 class="oppija">
    <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
    <span class="hetu">{oppijaHenkilö.hetuStr}</span>
  </h3>

  def oppiaineetHeaderHtml: Elem =
    <tr>
      <th class="oppiaine">Yhteiset ja niihin liittyvät valinnaiset oppiaineet</th>
      <th class="laajuus">Vuosiviikko- tuntimäärä</th>
      <th class="arvosana">Arvosana</th>
    </tr>

  def numeerinenArvosanaHtml(suoritus: Suoritus) = <span>
    {i(suoritus.arvosanaNumeroin)} {if (onYksilöllistetty(suoritus)) { <em class="yksilollistetty">*</em> }}
  </span>

  def oppiainerivitHtml(oppiaine: Aine, nimi: String, rowClass: String): Elem = {
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
        { numeerinenArvosanaHtml(oppiaine.suoritus) }
      </td>
    </tr>
  }

  sealed trait Aine { def suoritus: T }
  case class Pakollinen(suoritus: T) extends Aine
  case class Valinnainen(suoritus: T) extends Aine
  case class LiittyväValinnainen(suoritus: T) extends Aine
}
