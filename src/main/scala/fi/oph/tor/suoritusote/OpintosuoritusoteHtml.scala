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

  def render(ht: TaydellisetHenkilötiedot, opiskeluoikeudet: List[Opiskeluoikeus]) = {
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/tor/css/opintosuoritusote.css"></link>
        <style>{ indentCss }</style>
      </head>
      <body>
        <div class="opintosuoritusote">
          <section>
            <h3 class="title">Opintosuoritusote</h3>
            <div class="date-now">{dateFormatter.format(LocalDate.now)}</div>
          </section>

          <section class="henkilo">
            <div class="nimi">{ht.sukunimi} {ht.etunimet}</div>
            <div class="opiskelija"><div class="hetu">{ht.hetu}</div></div>
          </section>


          {
          ensisijainenOpiskeluoikeus(opiskeluoikeudet).toList.map { ensisijainen =>
            <section>
              <h3>Ensisijainen opinto-oikeus</h3>
              <table class="ensisijainen-opiskeluoikeus">
                <tr>
                  <td>Tavoitetutkinto</td>
                  <td>{i(ensisijainen.suoritukset.find(_.koulutusmoduuli.isTutkinto).map(_.koulutusmoduuli))}</td>
                </tr>
                <tr>
                  <td>Voimassa</td>
                  <td>{ensisijainen.ensisijaisuus.toList.map(e => dateFormatter.format(e.alkamispäivä) + " - " + e.päättymispäivä.map(dateFormatter.format(_)).getOrElse(""))}</td>
                </tr>
              </table>
            </section>
          }
          }

          <section>
            <h3>Suoritetut tutkinnot</h3>
            <table class="tutkinnot">
              { opiskeluoikeudet.flatMap(tutkinnot) }
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
              { opiskeluoikeudet.flatMap(suoritukset) }
            </table>
          </section>
        </div>
      </body>
    </html>
  }

  def ensisijainenOpiskeluoikeus(opiskeluoikeudet: List[Opiskeluoikeus]): Option[KorkeakoulunOpiskeluoikeus] = {
    opiskeluoikeudet.collect { case oo: KorkeakoulunOpiskeluoikeus => oo }
      .find(_.ensisijaisuus.exists { _.päättymispäivä match {
        case None => true
        case Some(pp) => pp.isAfter(LocalDate.now())
      }
      })
  }

  def tutkinnot(oo: Opiskeluoikeus) = oo.suoritukset.filter(s => s.tila.koodiarvo == "VALMIS" && s.koulutusmoduuli.isTutkinto).map { t =>
    <tr>
      <td>{t.koulutusmoduuli.tunniste.koodiarvo}</td>
      <td>{i(t.koulutusmoduuli)}</td>
      <td class="laajuus">{t.koulutusmoduuli.laajuus.map(l => decimalFormat.format(0)).getOrElse("")}</td>
      <td class="suoritus-pvm">{t.arviointi.flatMap(_.lastOption.flatMap(_.päivä.map(dateFormatter.format(_)))).getOrElse("")}</td>
    </tr>
  }

  private def suoritukset(opiskeluoikeus: Opiskeluoikeus) = {
    opiskeluoikeus.suoritukset.filter(s => s.tila.koodiarvo == "VALMIS").flatMap(suoritus => suoritusWithDepth((0, suoritus))).map(suoritusHtml)
  }

  private def suoritusHtml(t: (Int, Suoritus)) = t match { case (depth, suoritus) =>
    <tr>
      <td class={"depth-" + depth}>{suoritus.koulutusmoduuli.tunniste.koodiarvo}</td>
      <td class={"depth-" + depth}>{i(suoritus.koulutusmoduuli)}</td>
      <td class="laajuus">{suoritus.koulutusmoduuli.laajuus.map(l => decimalFormat.format(l.arvo)).getOrElse("")}</td>
      <td class="arvosana">{i(suoritus.arvosanaKirjaimin)}</td>
      <td class="suoritus-pvm">{suoritus.arviointi.flatMap(_.lastOption.flatMap(_.päivä.map(dateFormatter.format(_)))).getOrElse("")}</td>
    </tr>
  }

  private def suoritusWithDepth(t: (Int, Suoritus)) : List[(Int, Suoritus)] = {
    t :: t._2.osasuoritusLista.flatMap(s => suoritusWithDepth((t._1 + 1, s)))
  }

  private def indentCss = 0 to 5 map { i => ".depth-" + i + " { padding-left:" + (0.5 * i) + "em; }" } mkString("\n")

  def lang = user.lang
  private def i(s: Localizable): String = s.description.get(lang)
  private def i(s: Option[Localizable]): String = s.map(i).getOrElse("")

}
