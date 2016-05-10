package fi.oph.tor.suoritusote

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import fi.oph.tor.localization.Locale._
import fi.oph.tor.localization.Localizable
import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser

class OpintosuoritusoteHtml(implicit val user: TorUser) {

  val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
  val decimalFormat = NumberFormat.getInstance(finnish)

  def render(ht: TaydellisetHenkilötiedot, oo: Opiskeluoikeus) = {
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/tor/css/opintosuoritusote.css"></link>
        <style>{ indentCss }</style>
      </head>
      <body class="opintosuoritusote">

        <section>
          <h3 class="title">Opintosuoritusote</h3>
          <div class="date-now">{dateFormatter.format(LocalDate.now)}</div>
        </section>

        <section class="henkilo">
          <div class="nimi">{ht.sukunimi} {ht.etunimet}</div>
          <div class="opiskelija"><div class="hetu">{ht.hetu}</div></div>
        </section>

        <section>
          <h3>Suoritetut tutkinnot</h3>
          <table class="tutkinnot">
            <tr>
              <th class="tunnus"></th>
              <th class="nimi"></th>
            </tr>
            { tutkinnot(oo) }
          </table>
        </section>

        <section>
          <h3 class="suoritukset-title">Opintosuoritukset</h3>
          <table class="suoritukset">
            <tr>
              <th class="tunnus"></th>
              <th class="nimi"></th>
              <th class="laajuus">Op</th>
              <th class="arvosana">Arv.</th>
              <th class="suoritus-pvm">Suor.pvm</th>
            </tr>
            { suoritukset(oo) }
          </table>
        </section>

      </body>
    </html>
  }

  def tutkinnot(oo: Opiskeluoikeus) = oo.suoritukset.filter(_.tila.koodiarvo == "VALMIS").map { t =>
    <tr>
      <td>{t.koulutusmoduuli.tunniste.koodiarvo}</td>
      <td>{i(t.koulutusmoduuli)}</td>
      <td class="laajuus">{t.koulutusmoduuli.laajuus.map(l => decimalFormat.format(0)).getOrElse("")}</td>
      <td class="suoritus-pvm">{t.arviointi.flatMap(_.lastOption.flatMap(_.päivä.map(dateFormatter.format(_)))).getOrElse("")}</td>
    </tr>
  }

  private def suoritukset(opiskeluoikeus: Opiskeluoikeus) = opiskeluoikeus.suoritukset.headOption.map(tutkinto =>
    suoritusWithDepth(0, tutkinto).tail.flatMap {
      case (depth, suoritus) =>
        <tr>
          <td class={"depth-" + depth}>{suoritus.koulutusmoduuli.tunniste.koodiarvo}</td>
          <td class={"depth-" + depth}>{i(suoritus.koulutusmoduuli)}</td>
          <td class="laajuus">{suoritus.koulutusmoduuli.laajuus.map(l => decimalFormat.format(l.arvo)).getOrElse("")}</td>
          <td class="arvosana">{i(suoritus.arvosanaKirjaimin)}</td>
          <td class="suoritus-pvm">{suoritus.arviointi.flatMap(_.lastOption.flatMap(_.päivä.map(dateFormatter.format(_)))).getOrElse("")}</td>
        </tr>
    }
  ).toList.flatten

  private def suoritusWithDepth(t: (Int, Suoritus)) : List[(Int, Suoritus)] = {
    t :: t._2.osasuoritusLista.flatMap(s => suoritusWithDepth((t._1 + 1, s)))
  }

  private def indentCss = 1 to 5 map { i => ".depth-" + i + " { padding-left:" + (0.5 * i - 0.5) + "em; }" } mkString("\n")

  def lang = user.lang
  private def i(s: Localizable): String = s.description.get(lang)
  private def i(s: Option[Localizable]): String = s.map(i).getOrElse("")

}
