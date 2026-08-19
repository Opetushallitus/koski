package fi.oph.koski.sso

import fi.oph.koski.cas.CasClient.CasVirkailija
import fi.oph.koski.cas.CasLogout
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer.writeWithRoot
import fi.oph.koski.koskiuser.{AuthenticationUser, DirectoryClientLogin, KoskiCookieAndBasicAuthenticationSupport, UserLanguage}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.servlet.{NoCache, VirkailijaHtmlServlet}
import fi.oph.koski.huoltaja.HuollettavienHakuOnnistui
import org.scalatra.{Cookie, CookieOptions}

import java.net.URLEncoder.encode

/**
  *  This is where the user lands after a CAS login / logout
  */
class CasServlet()(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet with KoskiCookieAndBasicAuthenticationSupport with NoCache {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  private val koskiSessions = application.koskiSessionRepository
  protected val casService: CasService = application.casService
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
              val huollettavat = application.huoltajaServiceVtj.getHuollettavat(oppija)
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
            val kansalaisenTunnisteet = casService.validateKansalainenServiceTicket(url, ticket)
            oppijaCreation.findOrCreateByOidOrHetu(request, kansalaisenTunnisteet) match {
              case Some(oppija) =>
                val huollettavat = Some(oppija)
                  .map(application.huoltajaServiceVtj.getHuollettavat)
                val user = AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", serviceTicket = Some(ticket), kansalainen = true, huollettavat = huollettavat)
                koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
                UserLanguage.setLanguageCookie(UserLanguage.getLanguageFromLDAP(user, application.directoryClient).getOrElse(UserLanguage.getLanguageFromCookie(request)), response)
                setUser(Right(user))
                redirect(onSuccess)
              case None =>
                eiSuorituksia(kansalaisenTunnisteet)
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
          val casVirkailija = casService.validateVirkailijaServiceTicket(casVirkailijaServiceUrl, ticket)
          logger.debug(s"CAS virkailija service ticket validated: username=${casVirkailija.username}, kayttajaTyyppi=${casVirkailija.kayttajaTyyppi}, ticket=$ticket")
          handleVirkailijaLogin(ticket, casVirkailija)
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

  private def handleVirkailijaLogin(ticket: String, casVirkailija: CasVirkailija) = {
    if (isPalvelukayttaja(casVirkailija.kayttajaTyyppi)) {
      rejectVirkailijaLogin(s"Service account ${casVirkailija.username} attempted browser login and was rejected")
    } else {
      loginVirkailija(ticket, casVirkailija.username)
    }
  }

  private def loginVirkailija(ticket: String, username: String) = {
    DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
      case Some(user) =>
        startVirkailijaSession(ticket, user)
      case None =>
        rejectVirkailijaLogin(s"User $username not found even though user logged in with valid ticket")
    }
  }

  private def startVirkailijaSession(ticket: String, user: AuthenticationUser) = {
    setUser(Right(user.copy(serviceTicket = Some(ticket))))
    logger.info(s"Started session ${session.id} for ticket $ticket")
    koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
    UserLanguage.setLanguageCookie(UserLanguage.getLanguageFromLDAP(user, application.directoryClient).getOrElse(UserLanguage.getLanguageFromCookie(request)), response)
    redirectAfterLogin
  }

  private def rejectVirkailijaLogin(warning: String) = {
    logger.warn(warning)
    redirectToVirkailijaLogout
  }

  private def isPalvelukayttaja(kayttajaTyyppi: Option[String]): Boolean =
    kayttajaTyyppi.exists(_.trim.equalsIgnoreCase("PALVELU"))

  private def eiSuorituksia(kansalaisenTunnisteet: KansalaisenTunnisteet) = {
    setNimitiedotCookie(kansalaisenTunnisteet.nimi)
    redirect(onUserNotFound)
  }

  private def setNimitiedotCookie(altNimi: Option[String]): Unit = {
    val nimi = oppijaCreation
      .nimitiedot(request)
      .map(n => n.etunimet + " " + n.sukunimi)
      .filter(_.trim.nonEmpty)
      .orElse(altNimi)
    setCookie("koskiEiSuorituksiaNimi", nimi.getOrElse(""))
  }
}
