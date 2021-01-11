package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{Http, KoskiErrorCategory, OpintopolkuCallerId}
import fi.oph.koski.koskiuser.{AuthenticationSupport, AuthenticationUser, DirectoryClientLogin, KoskiUserLanguage}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.servlet.{NoCache, VirkailijaHtmlServlet}
import cas.{CasClient, CasClientException}
import cas.CasClient.{OppijaAttributes, Username}
import scalaz.concurrent.Task

import scala.util.control.NonFatal

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
            val hetu = validateServiceTicket(casOppijaClient, casOppijaServiceUrl, ticket, false)
            val oppija = application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu).get
            val huollettavat = application.huoltajaServiceVtj.getHuollettavat(hetu)
            val authUser = AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", None, kansalainen = true, huollettavat = Some(huollettavat))
            setUser(Right(localLogin(authUser, Some(langFromCookie.getOrElse(langFromDomain)))))
            redirect("/omattiedot")
          } else {
            val username = validateServiceTicket(casClient, casVirkailijaServiceUrl, ticket, true)
            DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
              case Some(user) =>
                setUser(Right(user.copy(serviceTicket = Some(ticket))))
                logger.info(s"Started session ${session.id} for ticket $ticket")
                koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
                KoskiUserLanguage.setLanguageCookie(KoskiUserLanguage.getLanguageFromLDAP(user, application.directoryClient), response)
                redirectAfterLogin
              case None =>
                logger.warn(s"User $username not found even though user logged in with valid ticket")
                redirectToVirkailijaLogout
            }
          }
        } catch {
          case e: Exception =>
            logger.warn(e)(s"Service ticket validation failed, ${e.toString}")
            haltWithStatus(KoskiErrorCategory.internalError("Sisäänkirjautuminen Opintopolkuun epäonnistui."))
        }
      case None =>
        // Seems to happen with Haka login. Redirect to login seems to cause another redirect to here with the required "ticket" parameter present.
        redirectAfterLogin
    }
  }

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

  def validateServiceTicket(client: CasClient, service: String, ticket: String, virkailija: Boolean): Username = {
    if (virkailija) {
      val attrs: Either[Throwable, Username] = client.validateServiceTicket(service)(ticket, casOppijaClient.decodeVirkailijaUsername).handleWith {
        case NonFatal(t) => Task.fail(new CasClientException(s"Failed to validate service ticket $t"))
      }.unsafePerformSyncAttemptFor(10000).toEither
      logger.debug(s"attrs response: $attrs")
      attrs match {
        case Right(attrs) => {
          attrs
        }
        case Left(t) => {
          throw new CasClientException(s"Unable to process CAS Virkailija login request, username cannot be resolved from ticket $ticket")
        }
      }
    }
    else {
      val attrs: Either[Throwable, OppijaAttributes] = client.validateServiceTicket(service)(ticket, casOppijaClient.decodeOppijaAttributes).handleWith {
        case NonFatal(t) => Task.fail(new CasClientException(s"Failed to validate service ticket $t"))
      }.unsafePerformSyncAttemptFor(10000).toEither
      logger.debug(s"attrs response: $attrs")
      attrs match {
        case Right(attrs) => {
          val hetu = attrs("nationalIdentificationNumber")
          println(attrs.toString())
          hetu
        }
        case Left(t) => {
          throw new CasClientException(s"Unable to process CAS Oppija login request, hetu cannot be resolved from ticket $ticket")
        }
      }
    }
  }
}
