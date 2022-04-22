package fi.oph.koski.sso

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer.writeWithRoot
import fi.oph.koski.koskiuser.{AuthenticationUser, DirectoryClientLogin, KoskiSpecificAuthenticationSupport, UserLanguage}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.servlet.{NoCache, VirkailijaHtmlServlet}
import fi.vm.sade.utils.cas.CasLogout
import org.scalatra.{Cookie, CookieOptions}

import java.net.URLEncoder.encode

/**
  *  This is where the user lands after a CAS login / logout
  */
class CasServlet()(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet with KoskiSpecificAuthenticationSupport with NoCache {

  def allowFrameAncestors: Boolean = Environment.isLocalDevelopmentEnvironment(application.config)

  private val koskiSessions = application.koskiSessionRepository
  private val casService = application.casService
  private val oppijaCreation = application.casOppijaCreationService

  protected def onSuccess: String = params.get("onSuccess").getOrElse("/koski/omattiedot")
  protected def onFailure: String = params.get("onFailure").getOrElse("/koski/virhesivu")
  protected def onUserNotFound: String = params.get("onUserNotFound").getOrElse("/koski/eisuorituksia")

  get("/oppija")(nonce => {
    if (application.config.getString("login.security") == "mock") {
      request.header("hetu") match {
        case Some(hetu) =>
          oppijaCreation.findOrCreate(request, hetu) match {
            case Some(oppija) =>
              val huollettavat = application.huoltajaServiceVtj.getHuollettavat(hetu)
              val user = AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", None, kansalainen = true, huollettavat = Some(huollettavat))
              val mockAuthUser =  localLogin(user, Some(langFromCookie.getOrElse(langFromDomain)))
              setUser(Right(mockAuthUser))
              redirect(onSuccess)
            case None => redirect(onFailure)
          }
        case None => redirect(onFailure)
      }
    } else {
      params.get("ticket") match {
        case Some(ticket) =>
          try {
            val url = params.get("onSuccess") match {
              case Some(onSuccessRedirectUrl) => casOppijaServiceUrl + "?onSuccess=" + onSuccessRedirectUrl
              case None => casOppijaServiceUrl
            }
            val hetu = casService.validateKansalainenServiceTicket(url, ticket)
            oppijaCreation.findOrCreate(request, hetu) match {
              case Some(oppija) =>
                val huollettavat = application.huoltajaServiceVtj.getHuollettavat(hetu)
                val user = AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", serviceTicket = Some(ticket), kansalainen = true, huollettavat = Some(huollettavat))
                koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
                UserLanguage.setLanguageCookie(UserLanguage.getLanguageFromLDAP(user, application.directoryClient), response)
                setUser(Right(user))
                redirect(onSuccess)
              case None =>
                eiSuorituksia
            }
          } catch {
            case e: Exception =>
              logger.warn(e)(s"Oppija login ticket validation failed, ${e.toString}")
              haltWithStatus(KoskiErrorCategory.internalError("Sisäänkirjautuminen Opintopolkuun epäonnistui."))
          }
        case None =>
          redirectAfterLogin
      }
    }
  })

  get("/virkailija")(nonce => {
    params.get("ticket") match {
      case Some(ticket) =>
        try {
          val username = casService.validateVirkailijaServiceTicket(casVirkailijaServiceUrl, ticket)
          DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
            case Some(user) =>
              setUser(Right(user.copy(serviceTicket = Some(ticket))))
              logger.info(s"Started session ${session.id} for ticket $ticket")
              koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
              UserLanguage.setLanguageCookie(UserLanguage.getLanguageFromLDAP(user, application.directoryClient), response)
              redirectAfterLogin
            case None =>
              logger.warn(s"User $username not found even though user logged in with valid ticket")
              redirectToVirkailijaLogout
          }
        } catch {
          case e: Exception =>
            logger.warn(e)(s"Virkailija login ticket validation failed, ${e.toString}")
            haltWithStatus(KoskiErrorCategory.internalError("Sisäänkirjautuminen Opintopolkuun epäonnistui."))
        }
      case None =>
        // Seems to happen with Haka login. Redirect to login seems to cause another redirect to here with the required "ticket" parameter present.
        redirectAfterLogin
    }
  })

  // Return url for cas logout
  post("/*") {
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

  private def eiSuorituksia = {
    setNimitiedotCookie
    redirect(onUserNotFound)
  }

  private def setNimitiedotCookie = {
    val name = oppijaCreation.nimitiedot(request).map(n => n.etunimet + " " + n.sukunimi)
    response.addCookie(Cookie("eisuorituksia", encode(writeWithRoot(name), "UTF-8"))(CookieOptions(secure = isHttps, path = "/", maxAge = application.sessionTimeout.seconds, httpOnly = true)))
  }
}
