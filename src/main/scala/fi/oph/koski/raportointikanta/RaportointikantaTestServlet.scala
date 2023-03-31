package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache, ObservableSupport}
import org.scalatra._

class RaportointikantaTestServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with NoCache with ObservableSupport with ContentEncodingSupport {
  private val service = new RaportointikantaService(application)

  before() {
    if (!isAuthenticated) {
      redirectToVirkailijaLogin
    }
    requireVirkailijaOrPalvelukäyttäjä
  }

  get("/load") {
    logger.info("load raportointikanta")
    service.loadRaportointikanta(
      force = getOptionalBooleanParam("force").getOrElse(false),
      skipUnchangedData = !getOptionalBooleanParam("fullReload").getOrElse(false),
      pageSize = OpiskeluoikeusLoader.LocalTestingBatchSize
    )
    renderObject(Map("status" -> "loading"))
  }

  get("/henkilot") {
    renderObject(Map("count" -> service.loadHenkilöt()))
  }

  get("/organisaatiot") {
    renderObject(Map("count" -> service.loadOrganisaatiot()))
  }

  get("/koodistot") {
    renderObject(Map("count" -> service.loadKoodistot()))
  }
}
