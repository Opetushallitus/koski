package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.schema._

import scala.xml.Elem

class NäyttötutkintoonValmentavanKoulutuksenTodistusHtml(val koulutustoimija: Option[OrganisaatioWithOid], val oppilaitos: Oppilaitos, val oppijaHenkilö: Henkilötiedot, val todistus: NäyttötutkintoonValmistavanKoulutuksenSuoritus)(implicit val user: KoskiUser) extends TodistusHtml {
  def title: String = "Näyttötutkintoon valmistavan koulutuksen osallistumistodistus"
  private def osat = todistus.osasuoritukset.toList.flatten

  def todistusHtml: Elem = {
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-ammatillinen-perustutkinto.css"></link>
      </head>
      <body>
        <div class="todistus">
          <h2 class="oppilaitos">{i(oppilaitos.nimi)}</h2>
          <h1>{title}</h1>
          <h3 class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">{oppijaHenkilö.hetu}</span>
          </h3>
          {
            val dates = (todistus.alkamispäivä.toList ++ todistus.päättymispäivä.toList).map(dateFormatter.format).mkString("-")
            val tutkinnonKuvaus = (todistus.koulutusmoduuli.tunniste.nimi.toList ++ todistus.osaamisala.toList.flatten.flatMap(_.nimi) ++ todistus.tutkintonimike.toList.flatten.flatMap(_.nimi)).map(i).mkString(", ")

            <p>on osallistunut { tutkinnonKuvaus}, valmistavaan koulutukseen { dates } seuraavilta osin: </p>
          }
          <h4>Koulutuksen sisällöt</h4>
          <table class="tutkinnon-osat">
            {suoritukset}
          </table>
          <p>Tutkintotodistuksen saamiseksi on osoitettava tukinnon perusteissa edellytetty ammattitaito tutkintotilaisuuksissa tutkintotoimikunnan valvonnassa. Tutkintotoimikunta antaa tutkintotodistuksen erikseen.</p>
          { todistus.vahvistus.toList.map(vahvistusHTML)}
        </div>
      </body>
    </html>
  }

  val suoritukset = osat.map { suoritus =>
    <tr class="tutkinnon-osa">
      <td class="nimi">{i(suoritus.koulutusmoduuli.nimi)}</td>
    </tr>
  }
}
