package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiCookieAndBasicAuthenticationSupport
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache, ObservableSupport}
import org.scalatra._

class RaportointikantaStatusServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with KoskiCookieAndBasicAuthenticationSupport with NoCache with ObservableSupport with ContentEncodingSupport {
  private val service = new RaportointikantaService(application)

  before() {
    if (!isAuthenticated) {
      redirectToVirkailijaLogin
    }
    requireVirkailijaOrPalvelukäyttäjä
  }
  get("/status") {
    renderObject(service.status)
  }
}
