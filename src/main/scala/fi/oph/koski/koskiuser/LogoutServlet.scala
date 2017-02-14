package fi.oph.koski.koskiuser

import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport

class LogoutServlet(val application: UserAuthenticationContext) extends HtmlServlet with SSOSupport {
  get("/") {
    logger.info("Logged out")
    getUser.right.toOption.flatMap(_.serviceTicket).foreach(application.koskiSessionRepository.removeSessionByTicket(_))
    removeUserCookie
    redirectToLogout
  }
}
