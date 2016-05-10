package fi.oph.tor.suoritusote

import java.text.NumberFormat
import java.time.format.DateTimeFormatter

import fi.oph.tor.localization.Locale._
import fi.oph.tor.localization.Localizable
import fi.oph.tor.schema.{Opiskeluoikeus, Suoritus, TaydellisetHenkilötiedot}
import fi.oph.tor.toruser.TorUser

class OpintosuoritusoteHtml(implicit val user: TorUser) {

  val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
  val decimalFormat = NumberFormat.getInstance(finnish)
  def lang = user.lang

  def render(ht: TaydellisetHenkilötiedot, oo: Opiskeluoikeus) = {
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/tor/css/opintosuoritusote.css"></link>
        <style>
          .depth-1 {{ padding-left:0.5em; }}
          .depth-2 {{ padding-left:1em; }}
          .depth-3 {{ padding-left:1.5em; }}
        </style>
      </head>
      <body class="opintosuoritusote">
        <h1>Opintosuoritusote</h1>
        <h3>Opintosuoritukset</h3>

        <table>
          <tr>
            <th class="tunnus"></th>
            <th class="nimi"></th>
            <th class="laajuus">Op</th>
            <th class="laajuus">Arv.</th>
            <th class="suoritus-pvm">Suor.pvm</th>
          </tr>
          { suoritusLista(oo) }
        </table>
      </body>
    </html>
  }

  private def suoritusWithDepth(t: (Int, Suoritus)) : List[(Int, Suoritus)] = {
    t :: t._2.osasuoritusLista.flatMap(s => suoritusWithDepth((t._1 + 1, s)))
  }

  private def suoritusLista(opiskeluoikeus: Opiskeluoikeus) = opiskeluoikeus.suoritukset.headOption.map(tutkinto =>
    suoritusWithDepth(0, tutkinto).flatMap {
      case (depth, suoritus) =>
        <tr>
          <td class={"depth-" + depth}>{suoritus.koulutusmoduuli.tunniste.koodiarvo}</td>
          <td class={"depth-" + depth}>{i(suoritus.koulutusmoduuli)}</td>
          <td>{suoritus.koulutusmoduuli.laajuus.map(l => decimalFormat.format(l.arvo)).getOrElse("")}</td>
          <td>{i(suoritus.arvosanaKirjaimin)}</td>
          <td>{suoritus.arviointi.flatMap(_.lastOption.flatMap(_.päivä.map(dateFormatter.format(_)))).getOrElse("")}</td>
        </tr>
    }
  ).toList.flatten

  private def i(s: Localizable): String = s.description.get(lang)
  private def i(s: Option[Localizable]): String = s.map(i).getOrElse("")

}
