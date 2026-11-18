package fi.oph.koski.raportit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport

class RaportitServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ContentEncodingSupport {
  private lazy val raportitService = new RaportitService(application)
  private lazy val organisaatioService = application.organisaatioService

  before() {
    if (!application.raportointikantaService.isAvailable) {
      haltWithStatus(KoskiErrorCategory.unavailable.raportit())
    }
    if (!session.hasRaportitAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
    }
  }

  get("/organisaatiot-ja-raporttityypit") {
    raportitService.getRaportinOrganisatiotJaRaporttiTyypit(organisaatioService.kaikkiKäyttöoikeudellisetOrganisaatiot)
  }

  get("/paivitysaika") {
    raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika
  }
}
