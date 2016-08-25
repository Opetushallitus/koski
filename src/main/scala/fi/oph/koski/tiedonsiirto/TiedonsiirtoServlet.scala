package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.ExamplesPerusopetus
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema._
import fi.oph.koski.servlet.ApiServlet

class TiedonsiirtoServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication {
  import TiedonsiirtoRivi._
  get() {
    List(virheetön, virheellinen)
  }
  get("/virheet") {
    List(virheellinen)
  }
}

case class TiedonsiirtoRivi(aika: String, oppija: Option[Henkilö], oppilaitos: Option[OrganisaatioWithOid], virhe: Option[AnyRef], inputData: Option[AnyRef])

object TiedonsiirtoRivi {
  val data = ExamplesPerusopetus.ysiluokkalainen
  private val eero: UusiHenkilö = UusiHenkilö("010101-123N", "Eero", "Eero", "Esimerkki")
  private val jyväskylänNormaalikoulu = Some(OidOrganisaatio("1.2.246.562.10.14613773812", Some(LocalizedString.finnish("Jyväskylän normaalikoulu"))))

  val virheellinen = TiedonsiirtoRivi("15.8.2016 11:32", Some(eero), jyväskylänNormaalikoulu, Some("Organisaatiota 1.2.246.562.100.24253545345 ei löydy organisaatiopalvelusta."), Some(data))
  val virheetön = TiedonsiirtoRivi("7.8.2016 9:08", Some(eero), jyväskylänNormaalikoulu, None, None)

}