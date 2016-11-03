package fi.oph.koski.koskiuser

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.vm.sade.utils.cas.CasLogout

class CasServlet(val application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache {
  private def validator = new ServiceTicketValidator(application.config)
  private val ticketSessions = application.serviceTicketRepository

  get("/") { // Return url for cas login
    params.get("ticket") match {
      case Some(ticket) =>
        try {
          val username = validator.validateServiceTicket(casServiceUrl, ticket)
          DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
            case Some(user) =>
              setUser(user.copy(serviceTicket = Some(ticket)))
              logger.info(s"Started session ${session.id} for ticket $ticket")
              ticketSessions.store(ticket, user)
              redirectAfterLogin
            case None =>
              haltWithStatus(KoskiErrorCategory.internalError(s"CAS-käyttäjää $username ei löytynyt LDAPista"))
          }
        } catch {
          case e: Exception =>
            logger.warn(e)("Service ticket validation failed")
            haltWithStatus(KoskiErrorCategory.internalError(s"CAS service ticket validation failure"))
        }
      case None =>
        // Seems to happen with Haka login. Redirect to login seems to cause another redirect to here with the required "ticket" parameter present.
        redirectAfterLogin
    }
  }

  post("/") { // Return url for cas logout
    params.get("logoutRequest") match {
      case Some(logoutRequest) =>
        CasLogout.parseTicketFromLogoutRequest(logoutRequest) match {
          case Some(parsedTicket) =>
            logger.info("Got CAS logout for ticket " + parsedTicket)
            ticketSessions.removeSessionByTicket(parsedTicket)
          case None =>
            logger.warn("Unable to parse CAS ticket from logout: " + logoutRequest)
        }
      case None =>
        logger.warn("Got CAS logout POST without logoutRequest parameter")
    }
  }
}

