package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AuthenticationSupport, DirectoryClientLogin, ServiceTicketValidator}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.servlet.{HtmlServlet, NoCache}
import fi.vm.sade.utils.cas.CasLogout

/**
  *  This is where the user lands after a CAS login / logout
  */
class CasServlet(val application: KoskiApplication) extends HtmlServlet with AuthenticationSupport with NoCache {
  private def validator = new ServiceTicketValidator(application.config)
  private val koskiSessions = application.koskiSessionRepository

  // Return url for cas login
  get("/") {
    params.get("ticket") match {
      case Some(ticket) =>
        try {
          val username = validator.validateServiceTicket(casServiceUrl, ticket)
          DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
            case Some(user) =>
              setUser(Right(user.copy(serviceTicket = Some(ticket))))
              logger.info(s"Started session ${session.id} for ticket $ticket")
              koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request))
              redirectAfterLogin
            case None =>
              haltWithStatus(KoskiErrorCategory.internalError(s"CAS-käyttäjää $username ei löytynyt LDAPista"))
          }
        } catch {
          case e: Exception =>
            logger.warn(e)("Service ticket validation failed")
            haltWithStatus(KoskiErrorCategory.internalError("Sisäänkirjautuminen Opintopolkuun epäonnistui."))
        }
      case None =>
        // Seems to happen with Haka login. Redirect to login seems to cause another redirect to here with the required "ticket" parameter present.
        redirectAfterLogin
    }
  }

  // Return url for cas logout
  post("/") {
    params.get("logoutRequest") match {
      case Some(logoutRequest) =>
        CasLogout.parseTicketFromLogoutRequest(logoutRequest) match {
          case Some(parsedTicket) =>
            logger.info("Got CAS logout for ticket " + parsedTicket)
            koskiSessions.removeSessionByTicket(parsedTicket)
          case None =>
            logger.warn("Unable to parse CAS ticket from logout: " + logoutRequest)
        }
      case None =>
        logger.warn("Got CAS logout POST without logoutRequest parameter")
    }
  }
}

