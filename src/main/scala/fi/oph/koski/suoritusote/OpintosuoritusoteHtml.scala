package fi.oph.koski.suoritusote

import java.text.NumberFormat
import java.time.LocalDate

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.localization.Locale._
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml

import scala.xml.Node

class OpintosuoritusoteHtml(implicit val user: KoskiUser) extends LocalizedHtml {
  val decimalFormat = NumberFormat.getInstance(finnish)

  def lukio(ht: TaydellisetHenkilötiedot, opiskeluoikeudet: List[LukionOpiskeluoikeus]) = {
    bodyHtml(ht, <div>
      {
      val suoritukset: List[(Int, Suoritus)] = opiskeluoikeudet.flatMap(oo => {
        val oppiainesuoritukset: List[LukionOppiaineenSuoritus] = oo.suoritukset.flatMap(_.osasuoritukset.toList.flatten)
        suorituksetSyvyydellä(oppiainesuoritukset)
      })
      suorituksetHtml(suoritukset)
      }
    </div>)
  }


  def korkeakoulu(ht: TaydellisetHenkilötiedot, opiskeluoikeudet: List[KorkeakoulunOpiskeluoikeus]) = {
    def ensisijainenOpiskeluoikeus(opiskeluoikeudet: List[Opiskeluoikeus]): Option[KorkeakoulunOpiskeluoikeus] = {
      opiskeluoikeudet.collect { case oo: KorkeakoulunOpiskeluoikeus => oo }
        .find(_.ensisijaisuus.exists { _.päättymispäivä match {
        case None => true
        case Some(pp) => pp.isAfter(LocalDate.now())
      }
      })
    }

    def tutkinnotHtml(oo: Opiskeluoikeus) = oo.suoritukset.filter(s => s.tila.koodiarvo == "VALMIS" && s.koulutusmoduuli.isTutkinto).map { t =>
      <tr>
        <td>{t.koulutusmoduuli.tunniste.koodiarvo}</td>
        <td>{i(t.koulutusmoduuli)}</td>
        <td class="laajuus">{t.koulutusmoduuli.laajuus.map(l => decimalFormat.format(0)).getOrElse("")}</td>
        <td class="suoritus-pvm">{t.arviointi.flatMap(_.lastOption.flatMap(_.arviointipäivä.map(dateFormatter.format(_)))).getOrElse("")}</td>
      </tr>
    }

    bodyHtml(ht, <div>
      {
        ensisijainenOpiskeluoikeus(opiskeluoikeudet).toList.map { ensisijainen =>
          <section class="opiskeluoikeus">
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

      <section class="suoritetut-tutkinnot">
        <h3>Suoritetut tutkinnot</h3>
        <table class="tutkinnot">
          { opiskeluoikeudet.flatMap(tutkinnotHtml) }
        </table>
      </section>
      {
        val suoritukset: List[(Int, Suoritus)] = opiskeluoikeudet.flatMap(oo => suorituksetSyvyydellä(oo.suoritukset))
        suorituksetHtml(suoritukset)
      }
    </div>
    )
  }

  private def suorituksetHtml(suoritukset: List[(Int, Suoritus)]) = {
    def suoritusHtml(t: (Int, Suoritus)) = {
      def laajuus(suoritus: Suoritus) = if (suoritus.osasuoritukset.isDefined) {
        decimalFormat.format(suoritus.osasuoritusLista.foldLeft(0f) { (laajuus: Float, suoritus: Suoritus) =>
          laajuus + suoritus.koulutusmoduuli.laajuus.map(_.arvo).getOrElse(0f)
        })
      } else {
        suoritus.koulutusmoduuli.laajuus.map(l => decimalFormat.format(l.arvo)).getOrElse("")
      }

      t match { case (depth, suoritus) =>
        <tr>
          <td class={"depth-" + depth}>{suoritus.koulutusmoduuli.tunniste.koodiarvo}</td>
          <td class={"depth-" + depth}>{i(suoritus.koulutusmoduuli)}</td>
          <td class="laajuus">{laajuus(suoritus)}</td>
          <td class="arvosana">{i(suoritus.arvosanaNumeroin.getOrElse(suoritus.arvosanaKirjaimin))}</td>
          <td class="suoritus-pvm">{suoritus.arviointi.flatMap(_.lastOption.flatMap(_.arviointipäivä.map(dateFormatter.format(_)))).getOrElse("")}</td>
        </tr>
      }
    }

    val laajuudet: List[LocalizedString] = suoritukset.map(_._2).flatMap(_.koulutusmoduuli.laajuus.flatMap(laajuus => laajuus.yksikkö.lyhytNimi.orElse(laajuus.yksikkö.nimi)))
    val laajuusYksikkö = laajuudet.headOption

    <section class="opintosuoritukset">
      <h3 class="suoritukset-title">Opintosuoritukset</h3>
      <table class="suoritukset">
        <tr class="header">
          <th class="tunnus"></th>
          <th class="nimi"></th>
          <th class="laajuus">{ i(laajuusYksikkö).capitalize }</th>
          <th class="arvosana">Arvosana</th>
          <th class="suoritus-pvm">Suor.pvm</th>
        </tr>
        { suoritukset.map(suoritusHtml) }
      </table>
    </section>
  }


  private def bodyHtml(ht: TaydellisetHenkilötiedot, content: Node) = {
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/opintosuoritusote.css"></link>
        <style>{ indentCss }</style>
      </head>
      <body class="opintosuoritusote">
        <section>
          <h1 class="title">Opintosuoritusote</h1>
          <div class="date-now">{dateFormatter.format(LocalDate.now)}</div>
        </section>

        <section class="henkilo">
          <div class="nimi">{ht.sukunimi} {ht.etunimet}</div>
          <div class="opiskelija"><div class="hetu">{ht.hetu}</div></div>
        </section>

        { content }
      </body>
    </html>
  }


  private def suorituksetSyvyydellä(roots: List[Suoritus]): List[(Int, Suoritus)] = {
    def suoritusWithDepth(t: (Int, Suoritus)) : List[(Int, Suoritus)] = {
      t :: t._2.osasuoritusLista.sortBy(s => i(s.koulutusmoduuli.nimi)).flatMap(s => suoritusWithDepth((t._1 + 1, s)))
    }

    roots.filter(s => s.tila.koodiarvo == "VALMIS")
      .sortBy(s => (!s.koulutusmoduuli.isTutkinto, i(s.koulutusmoduuli.nimi)))
      .flatMap(suoritus => suoritusWithDepth((0, suoritus)))
  }


  private def indentCss = 0 to 5 map { i => ".depth-" + i + " { padding-left:" + (0.5 * i) + "em; }" } mkString("\n")
}
