package fi.oph.koski.todistus

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.schema._

import scala.xml.NodeSeq.Empty
import scala.xml.{Elem, NodeSeq}

trait ValmentavanKoulutuksenTodistusHtml extends TodistusHtml {
  def koulutustoimija: Option[OrganisaatioWithOid]
  def oppilaitos: Oppilaitos
  def title: String
  def oppijaHenkilö: Henkilötiedot
  def todistus: ValmentavaSuoritus
  private def oppiaineet = todistus.osasuoritukset.toList.flatten

  def todistusHtml: Elem = {
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-valmentava.css"></link>
        {styles}
      </head>
      <body>
        <div class="todistus">
          <h1>{title}</h1>
          <h2 class="koulutustoimija">{i(koulutustoimija.flatMap(_.nimi))}</h2>
          <h2 class="oppilaitos">{i(oppilaitos.nimi)}</h2>
          <h3 class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">{oppijaHenkilö.hetu}</span>
          </h3>
          <table class="tutkinnon-osat">
            {tutkinnonOtsikkoRivi}
            {tutkinnonOsat}
            <tr class="opintojen-laajuus">
              <td class="nimi">Opiskelijan suorittamien koulutuksen osien laajuus osaamispisteinä</td>
              <td class="laajuus">{decimalFormat.format(oppiaineet.map(laajuus).sum)}</td>
            </tr>
            {lisätietoja}
          </table>
          { todistus.vahvistus.toList.map(vahvistusHTML)}
        </div>
      </body>
    </html>
  }

  def styles: NodeSeq = Empty

  def tutkinnonOtsikkoRivi: Elem = <tr>
    <th class="oppiaine">Koulutuksen osat</th>
    <th class="laajuus">Suoritettu laajuus, osp</th>
    <th colspan="2" class="arvosana">Arvosana</th>
  </tr>

  def tutkinnonOsat = oppiaineetTyypeittäin.flatMap { case (tyyppi, suoritukset: List[ValmentavanKoulutuksenOsanSuoritus]) =>
      <tr class="rakennemoduuli">
        <td class="oppiaine">{i(tyyppi)} {decimalFormat.format(suoritukset.map(laajuus).sum)} osp</td>
      </tr> :: tutkinnonOsaRivit(suoritukset)
  }

  def tutkinnonOsaRivit(suoritukset: List[ValmentavanKoulutuksenOsanSuoritus]): List[Elem] = suoritukset.map { oppiaine =>
    <tr class="tutkinnon-osa">
      <td class="nimi">{nimiTeksti(oppiaine)}</td>
      <td class="laajuus">{decimalFormat.format(laajuus(oppiaine))}</td>
      <td class="arvosana-kirjaimin">{i(oppiaine.arvosanaKirjaimin).capitalize}</td>
      <td class="arvosana-numeroin">{i(oppiaine.arvosanaNumeroin)}</td>
    </tr>
  }

  def nimiTeksti(oppiaine: ValmentavanKoulutuksenOsanSuoritus): String =
    i(oppiaine.koulutusmoduuli) + lisätiedotIndex.get(oppiaine).map(i => s" $i)").getOrElse("")

  private def lisätietoja = if (tunnustetut.nonEmpty) {
    <tr class="lisatietoja"><td>Lisätietoja:</td></tr> ::
    tunnustetut.map { oppiaine =>
      val tunnustettu = oppiaine.tunnustettu.get
      val vahvistus = tunnustettu.osaaminen.flatMap(_.vahvistus)
      val pvm = vahvistus.map(_.päivä).map(dateFormatter.format)
      val ammatillinenSuoritus = tunnustettu.osaaminen match {
        case Some(s: AmmatillisenTutkinnonOsanSuoritus) => Some(s)
        case _ => None
      }
      val diaariNumero = ammatillinenSuoritus.flatMap(_.tutkinto).flatMap(_.perusteenDiaarinumero).getOrElse("")
      val organisaatioNimi = vahvistus.map(_.myöntäjäOrganisaatio).collect {
        case o : OrganisaatioWithOid => o.nimi
        case Yritys(nimi, _) => Some(nimi)
        case Tutkintotoimikunta(nimi, _) => Some(nimi)
        case _ => None
      }.map(nimi => s", ${i(nimi)}").getOrElse("")

      <tr><td colspan="4">
        <span>{lisätiedotIndex.get(oppiaine).map(i => s"$i)").getOrElse("")}</span> {i(tunnustettu.selite)} ({pvm.map(p => s"$p, ").getOrElse("")}{diaariNumero}){organisaatioNimi}
      </td></tr>
    }
  } else Empty

  private val oppiaineetTyypeittäin: List[(LocalizedString, List[ValmentavanKoulutuksenOsanSuoritus])] =
    oppiaineet.groupBy(s => tyypinKuvaus(s.koulutusmoduuli)).toList.sortBy(_._1.get("fi"))

  private val tunnustetut: List[ValmentavanKoulutuksenOsanSuoritus] = oppiaineetTyypeittäin.flatMap(_._2).filter(_.tunnustettu.isDefined)

  private val lisätiedotIndex: Map[ValmentavanKoulutuksenOsanSuoritus, Int] =
    tunnustetut.zipWithIndex.map { case (oppiaine, index) => (oppiaine, index + 1) }.toMap

  private def tyypinKuvaus(km: Koulutusmoduuli) = km match {
    case o: Valinnaisuus if o.pakollinen => finnish("Pakolliset koulutuksen osat")
    case _ => finnish("Valinnaiset koulutuksen osat")
  }
}
