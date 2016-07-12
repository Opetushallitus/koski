package fi.oph.koski.koskiuser

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.servlet.ApiServlet
import fi.vm.sade.utils.cas.CasLogout

class CasServlet(val application: KoskiApplication) extends ApiServlet with AuthenticationSupport {
  private def validator = new ServiceTicketValidator(application.config)
  private val ticketSessions = new CasTicketSessionRepository(application.database.db)

  get("/") { // Return url for cas login
    params.get("ticket") match {
      case Some(ticket) =>
        try {
          val username = validator.validateServiceTicket(casServiceUrl, ticket)
          DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
            case Some(user) =>
              scentry.user = user.copy(serviceTicket = Some(ticket))
              logger.info(s"Started session ${session.id} for ticket $ticket")
              ticketSessions.store(session.id, ticket, user)
              setServiceTicketCookie(ticket)
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
    params.get("logoutRequest") match {
      case Some(logoutRequest) =>
        CasLogout.parseTicketFromLogoutRequest(logoutRequest) match {
          case Some(parsedTicket) =>
            logger.info("Got CAS logout for ticket " + parsedTicket)
            ticketSessions.removeSessionByTicket(parsedTicket) match {
              case Some(sessionId) =>
                logger.info(s"Invalidated session $sessionId for ticket $parsedTicket")
              case None =>
                logger.warn(s"Session id not found for ticket $parsedTicket")
            }
          case None =>
            logger.warn("Unable to parse CAS ticket from logout: " + logoutRequest)
        }
      case None =>
        logger.warn("Got CAS logout POST without logoutRequest parameter")
    }
  }
}

