package fi.oph.koski.koskiuser

import fi.oph.koski.servlet.HtmlServlet

class LogoutServlet(val application: UserAuthenticationContext) extends HtmlServlet {
  get("/") {
    logger.info("Logged out")
    Option(request.getSession(false)).foreach(_.invalidate())
    redirectToLogin
  }
}
