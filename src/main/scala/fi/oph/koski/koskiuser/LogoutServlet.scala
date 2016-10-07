package fi.oph.koski.koskiuser

import fi.oph.koski.servlet.{CasSingleSignOnSupport, HtmlServlet}

class LogoutServlet(val application: UserAuthenticationContext) extends HtmlServlet with CasSingleSignOnSupport {
  get("/") {
    logger.info("Logged out")
    getUser.right.toOption.flatMap(_.serviceTicket).foreach(application.serviceTicketRepository.removeSessionByTicket(_))
    removeUserCookie
    redirectToLogout
  }
}
