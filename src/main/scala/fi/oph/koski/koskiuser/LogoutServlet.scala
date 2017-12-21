package fi.oph.koski.koskiuser

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport

class LogoutServlet(implicit val application: KoskiApplication) extends HtmlServlet with SSOSupport {
  get("/") {
    logger.info("Logged out")
    getUser.right.toOption.flatMap(_.serviceTicket).foreach(application.koskiSessionRepository.removeSessionByTicket)
    val kansalainen = koskiSessionOption.exists(_.user.kansalainen)
    removeUserCookie
    if (kansalainen) {
      kansalaisLogout
    } else {
      redirectToLogout
    }
  }

  private def kansalaisLogout = {
    val shibbolethLogoutUrl = application.config.getString("shibboleth.logout.url")
    if (shibbolethLogoutUrl.isEmpty) {
      redirectToFrontpage
    } else {
      redirect(shibbolethLogoutUrl)
    }
  }
}
