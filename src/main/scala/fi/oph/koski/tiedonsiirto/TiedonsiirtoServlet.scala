package fi.oph.koski.tiedonsiirto

import java.time.LocalDateTime

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.ExamplesPerusopetus
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._
import MockOppijat._
import fi.oph.koski.servlet.ApiServlet
import fi.oph.koski.util.{DateOrdering, FinnishDateFormat}

class TiedonsiirtoServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication {
  import TiedonsiirtoRivi._
  get() {
    groupByOppija(kaikki)
  }
  get("/virheet") {
    groupByOppija(virheelliset)
  }

  def groupByOppija(rivit: List[TiedonsiirtoRivi]) = {
    implicit val ordering = DateOrdering.localDateTimeReverseOrdering
    rivit.groupBy(_.oppija).toList.map{ case (henkilö, rivit) => HenkilönTiedonsiirrot(henkilö, rivit.sortBy(_.aika))}.sortBy(_.rivit(0).aika)
  }
}

case class HenkilönTiedonsiirrot(oppija: Option[Henkilö], rivit: List[TiedonsiirtoRivi])
case class TiedonsiirtoRivi(aika: LocalDateTime, oppija: Option[Henkilö], oppilaitos: Option[OrganisaatioWithOid], virhe: Option[AnyRef], inputData: Option[AnyRef])

object TiedonsiirtoRivi {
  val data = ExamplesPerusopetus.ysiluokkalainen
  private val jyväskylänNormaalikoulu = Some(OidOrganisaatio("1.2.246.562.10.14613773812", Some(LocalizedString.finnish("Jyväskylän normaalikoulu"))))


  val kaikki = List(
    TiedonsiirtoRivi(d("15.8.2016 11:32"), Some(eero), jyväskylänNormaalikoulu, Some("Organisaatiota 1.2.246.562.100.24253545345 ei löydy organisaatiopalvelusta."), Some(data)),
    TiedonsiirtoRivi(d("14.8.2016 11:32"), Some(MockOppijat.markkanen), jyväskylänNormaalikoulu, None, None),
    TiedonsiirtoRivi(d("7.8.2016 9:08"), Some(eero), jyväskylänNormaalikoulu, None, None)
  )

  val virheelliset = kaikki.filter(_.virhe.isDefined)

  def d(s: String) = LocalDateTime.parse(s, FinnishDateFormat.finnishDateTimeFormat)

}