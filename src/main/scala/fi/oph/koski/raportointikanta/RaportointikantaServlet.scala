package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache, ObservableSupport}
import org.scalatra._

class RaportointikantaServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache with ObservableSupport with ContentEncodingSupport {
  private val service = new RaportointikantaService(application)

  before() {
    noRemoteCallsExpectFor("/status")
  }

  get("/load") {
    logger.info("load raportointikanta")
    service.loadRaportointikanta(
      force = getOptionalBooleanParam("force").getOrElse(false),
      skipUnchangedData = !getOptionalBooleanParam("fullReload").getOrElse(false),
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

  get("/status") {
    renderObject(service.status)
  }
}
