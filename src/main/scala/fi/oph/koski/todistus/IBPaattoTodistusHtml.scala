package fi.oph.koski.todistus


import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.schema._

import scala.xml.Elem
import scala.xml.NodeSeq.Empty


class IBPaattoTodistusHtml(implicit val user: KoskiUser) extends TodistusHtml {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, päättötodistus: IBTutkinnonSuoritus) = {
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-ib.css"></link>

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
              <th class="taso">Level</th>
              <th class="arvosana-kirjaimin">Grades in words</th>
              <th class="arvosana-numeroin">Grades in numbers</th>
            </tr>
            {
              val oppiaineet: List[IBOppiaineenSuoritus] = päättötodistus.osasuoritukset.toList.flatten
              oppiaineet.map(oppiaineRow)
            }
          </table>
          <div class="core-elements">
            {
              päättötodistus.theoryOfKnowledge.map { o =>
                <div class="theory-of-knowledge">
                  <span class="label">{i(o.koulutusmoduuli)}</span>
                  <span class="grade">{i(o.arvosanaKirjaimin)}</span>
                </div>
              }.getOrElse(Empty)
            }
            {
              päättötodistus.creativityActionService.map { o =>
                <div class="cas">
                  <span class="label">{i(o.koulutusmoduuli)}</span>
                  <span>{o.koulutusmoduuli.laajuus.map(l => decimalFormat.format(l.arvo)).getOrElse("")}</span>
                  <span class="grade">{i(o.arvosanaKirjaimin)}</span>
                </div>
              }.getOrElse(Empty)
            }
            {
              päättötodistus.extendedEssay.map { o =>
                <div class="extended-essay">
                  <div class="label">{i(o.koulutusmoduuli)}</div>
                  <table>
                    <tr><td class="label">Subject:</td><td>{i(o.koulutusmoduuli.aine)}</td></tr>
                    <tr><td class="label">Topic:</td><td>{i(o.koulutusmoduuli.aihe)}</td></tr>
                  </table>
                </div>
              }.getOrElse(Empty)
            }
          </div>
          { päättötodistus.vahvistus.toList.map(vahvistusHTML)}
        </div>
      </body>
    </html>
  }

  def oppiaineRow(oppiaine: IBOppiaineenSuoritus): Elem = {
    val nimiTeksti = i(oppiaine.koulutusmoduuli)
    val rowClass = "oppiaine " + oppiaine.koulutusmoduuli.tunniste.koodiarvo
    <tr class={rowClass}>
      <td class="oppiaine">{nimiTeksti}</td>
      <td class="taso">{oppiaine.koulutusmoduuli.taso.map(_.koodiarvo).getOrElse("")}</td>
      <td class="arvosana-kirjaimin">{i(oppiaine.arvosanaKirjaimin).capitalize}</td>
      <td class="arvosana-numeroin">{i(oppiaine.arvosanaNumeroin)}</td>
    </tr>
  }

  override def lang: String = "en"
}