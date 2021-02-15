package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.servlet.{ApiServlet, KoskiSpecificApiServlet, NoCache, ObservableSupport}
import org.scalatra._

class RaportointikantaServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache with ObservableSupport with ContentEncodingSupport {
  private val service = new RaportointikantaService(application)

  before() {
    noRemoteCallsExpectFor("/status")
  }

  get("/clear") {
    service.dropAndCreateSchema
    renderObject(Map("ok" -> true))
  }

  get("/load") {
    logger.info("load raportointikanta")
    service.loadRaportointikanta(getBooleanParam("force"))
    renderObject(Map("status" -> "loading"))
  }

  get("/opiskeluoikeudet") {
    streamResponse[LoadResult](service.loadOpiskeluoikeudet(), KoskiSpecificSession.systemUser)
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
