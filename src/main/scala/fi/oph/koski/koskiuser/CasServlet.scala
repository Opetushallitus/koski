package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.servlet.ApiServlet
import fi.vm.sade.utils.cas.CasLogout

class CasServlet(val application: UserAuthenticationContext) extends ApiServlet with AuthenticationSupport {
  private def validator = new ServiceTicketValidator(application.config)

  get("/") { // Return url for cas login
    params.get("ticket") match {
      case Some(ticket) =>
        try {
          val username = validator.validateServiceTicket(casServiceUrl, ticket)
          DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
            case Some(user) =>
              scentry.user = user
              redirect("/")
            case None =>
              haltWithStatus(KoskiErrorCategory.internalError(s"CAS-käyttäjää $username ei löytynyt LDAPista"))
          }
        } catch {
          case e: Exception =>
            logger.warn(e)("Service ticket validation failed")
            haltWithStatus(KoskiErrorCategory.internalError(s"CAS service ticket validation failure"))
        }
      case None =>
        haltWithStatus(KoskiErrorCategory.internalError("Got CAS login GET without ticket parameter"))
    }
  }

  post("/") { // Return url for cas logout
  val logoutRequest = params.get("logoutRequest") match {
      case Some(logoutRequest) =>
        val parsedTicket = CasLogout.parseTicketFromLogoutRequest(logoutRequest)
        logger.info("Got CAS logout for ticket " + parsedTicket)
      case None =>
        logger.warn("Got CAS logout POST without logoutRequest parameter")
    }
  }
}
