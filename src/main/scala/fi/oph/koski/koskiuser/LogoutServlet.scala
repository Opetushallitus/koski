package fi.oph.koski.koskiuser

import fi.oph.koski.servlet.HtmlServlet
import fi.vm.sade.security.ldap.DirectoryClient

class LogoutServlet(val käyttöoikeudet: KäyttöoikeusRepository, val directoryClient: DirectoryClient) extends HtmlServlet {
  get("/") {
    logger.info("Logged out")
    Option(request.getSession(false)).foreach(_.invalidate())
    redirectToLogin
  }
}
