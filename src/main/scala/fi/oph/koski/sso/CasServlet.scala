package fi.oph.koski.sso

import fi.oph.koski.cas.CasLogout
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer.writeWithRoot
import fi.oph.koski.koskiuser.{AuthenticationUser, DirectoryClientLogin, KoskiSpecificAuthenticationSupport, UserLanguage}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.schema.{Nimitiedot, UusiHenkilö}
import fi.oph.koski.servlet.{NoCache, VirkailijaHtmlServlet}
import org.scalatra.{Cookie, CookieOptions}

import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets

/**
  *  This is where the user lands after a CAS login / logout
  */
class CasServlet()(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet with KoskiSpecificAuthenticationSupport with NoCache {
  private val koskiSessions = application.koskiSessionRepository
  private val casService = application.casService

  protected def onSuccess: String = params.get("onSuccess").getOrElse("/omattiedot")
  protected def onFailure: String = params.get("onFailure").getOrElse("/virhesivu")
  protected def onUserNotFound: String = params.get("onUserNotFound").getOrElse("/eisuorituksia")

  get("/oppija") {
    if (application.config.getString("login.security") == "mock") {
      request.header("hetu") match {
        case Some(hetu) =>
          findOrCreate(hetu) match {
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
            findOrCreate(hetu) match {
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
  }

  get("/virkailija") {
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
  }

  private def findOrCreate(validHetu: String) = {
    application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(validHetu, nimitiedot)
      .orElse(nimitiedot.map(toUusiHenkilö(validHetu, _)).map(application.henkilöRepository.findOrCreate(_).left.map(s => new RuntimeException(s.errorString.mkString)).toTry.get))
  }

  private def toUusiHenkilö(validHetu: String, nimitiedot: Nimitiedot) = UusiHenkilö(
    hetu = validHetu,
    etunimet = nimitiedot.etunimet,
    kutsumanimi = Some(nimitiedot.kutsumanimi),
    sukunimi = nimitiedot.sukunimi
  )

  private def eiSuorituksia = {
    setNimitiedotCookie
    redirect(onUserNotFound)
  }

  private def setNimitiedotCookie = {
    val name = nimitiedot.map(n => n.etunimet + " " + n.sukunimi)
    response.addCookie(Cookie("eisuorituksia", encode(writeWithRoot(name), "UTF-8"))(CookieOptions(secure = isHttps, path = "/", maxAge = application.sessionTimeout.seconds, httpOnly = true)))
  }

  private def nimitiedot: Option[Nimitiedot] = {
    val nimi = for {
      etunimet <- utf8Header("FirstName")
      kutsumanimi <- utf8Header("givenName")
      sukunimi <- utf8Header("sn")
    } yield Nimitiedot(etunimet = etunimet, kutsumanimi = kutsumanimi, sukunimi = sukunimi)
    logger.debug(nimi.toString)
    nimi
  }

  private def utf8Header(headerName: String): Option[String] =
    request.header(headerName)
      .map(header => new String(header.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8))
      .map(_.trim)
      .filter(_.nonEmpty)

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
}
