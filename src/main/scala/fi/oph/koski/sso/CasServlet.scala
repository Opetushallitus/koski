package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{Http, KoskiErrorCategory, OpintopolkuCallerId}
import fi.oph.koski.koskiuser.{AuthenticationSupport, AuthenticationUser, DirectoryClientLogin, KoskiUserLanguage}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.servlet.{NoCache, VirkailijaHtmlServlet}
import cas.CasClient
import cas.CasClient.Username

/**
  *  This is where the user lands after a CAS login / logout
  */
class CasServlet()(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet with AuthenticationSupport with NoCache {
  private val casClient = new CasClient(application.config.getString("opintopolku.virkailija.url") + "/cas", Http.newClient("cas.serviceticketvalidation"), OpintopolkuCallerId.koski)
  private val casOppijaClient = new CasClient(application.config.getString("opintopolku.oppija.url") + "/cas-oppija", Http.newClient("cas.serviceticketvalidation"), OpintopolkuCallerId.koski)
  private val koskiSessions = application.koskiSessionRepository

  // Return url for cas login
  get("/*") {
    println(requestPath)
    val kansalainen = requestPath match {
      case "/virkailija" => false;
      case _ => true;
    };

    params.get("ticket") match {
      case Some(ticket) =>
        try {
          println(kansalainen)
          println(routeBasePath)
          if (kansalainen) {
            println("Oppijaserviceurl:")
            println(casOppijaServiceUrl)
            val username = validateServiceTicket(casOppijaClient, casOppijaServiceUrl, ticket)
            val hetu = username.split('#')(1)
            val oppija = application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu).get
            val huollettavat = application.huoltajaServiceVtj.getHuollettavat(hetu)
            val authUser = AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", None, kansalainen = true, huollettavat = Some(huollettavat))
            setUser(Right(localLogin(authUser, Some(langFromCookie.getOrElse(langFromDomain)))))
            redirect("/omattiedot")
          } else {
            val username = validateServiceTicket(casClient, casVirkailijaServiceUrl, ticket)
            DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
              case Some(user) =>
                setUser(Right(user.copy(serviceTicket = Some(ticket))))
                logger.info(s"Started session ${session.id} for ticket $ticket")
                koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
                KoskiUserLanguage.setLanguageCookie(KoskiUserLanguage.getLanguageFromLDAP(user, application.directoryClient), response)
                redirectAfterLogin
              case None =>
                logger.warn(s"User $username not found even though user logged in with valid ticket")
                redirectToLogout
            }
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

  /*
    private def findOrCreate(validHetu: String) = {
    application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(validHetu, nimitiedot)
      .orElse(nimitiedot.map(toUusiHenkilö(validHetu, _)).map(application.henkilöRepository.findOrCreate(_).left.map(s => new RuntimeException(s.errorString.mkString)).toTry.get))
  }

  private def createSession(oppija: OppijaHenkilö, hetu: String) = {
    val huollettavat = application.huoltajaServiceVtj.getHuollettavat(hetu)
    val authUser = AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", None, kansalainen = true, huollettavat = Some(huollettavat))
    setUser(Right(localLogin(authUser, Some(langFromCookie.getOrElse(langFromDomain)))))
    redirect(onSuccess)
  }
   */

  // Return url for cas logout
  post("/*") {
    params.get("logoutRequest") match {
      case Some(logoutRequest) =>
        cas.CasLogout.parseTicketFromLogoutRequest(logoutRequest) match {
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

  def validateServiceTicket(client: CasClient, service: String, ticket: String): Username =
    client.validateServiceTicket(service)(ticket).unsafePerformSync
}

