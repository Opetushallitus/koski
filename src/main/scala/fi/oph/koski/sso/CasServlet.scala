package fi.oph.koski.sso

import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets

import fi.oph.koski.config.{KoskiApplication}
import fi.oph.koski.http.{Http, KoskiErrorCategory, OpintopolkuCallerId}
import fi.oph.koski.koskiuser.{AuthenticationUser, DirectoryClientLogin, KoskiSpecificAuthenticationSupport, UserLanguage}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.servlet.{NoCache, VirkailijaHtmlServlet}
import fi.vm.sade.utils.cas.{CasClient, CasClientException, CasLogout}
import fi.vm.sade.utils.cas.CasClient.{OppijaAttributes, Username}
import fi.oph.koski.json.JsonSerializer.writeWithRoot
import scalaz.concurrent.Task
import fi.oph.koski.schema.{Nimitiedot, UusiHenkilö}
import org.scalatra.{Cookie, CookieOptions}

import scala.util.control.NonFatal

/**
  *  This is where the user lands after a CAS login / logout
  */
class CasServlet()(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet with KoskiSpecificAuthenticationSupport with NoCache {
  private val casVirkailijaClient = new CasClient(application.config.getString("opintopolku.virkailija.url") + "/cas", Http.newClient("cas.serviceticketvalidation"), OpintopolkuCallerId.koski)
  private val casOppijaClient = new CasClient(application.config.getString("opintopolku.oppija.url") + "/cas-oppija", Http.newClient("cas.serviceticketvalidation"), OpintopolkuCallerId.koski)
  private val koskiSessions = application.koskiSessionRepository
  private val mockUsernameForAllVirkailijaTickets = {
    if (application.config.getString("opintopolku.virkailija.url") == "mock" && application.config.hasPath("mock.casClient.usernameForAllVirkailijaTickets")) {
      Some(application.config.getString("mock.casClient.usernameForAllVirkailijaTickets"))
    } else {
      None
    }
  }

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
            val hetu = validateKansalainenServiceTicket(ticket)
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
          val username = validateVirkailijaServiceTicket(ticket)
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
    response.addCookie(Cookie("eisuorituksia", encode(writeWithRoot(name), "UTF-8"))(CookieOptions(secure = true, path = "/", maxAge = application.sessionTimeout.seconds, httpOnly = true)))
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

  def validateKansalainenServiceTicket(ticket: String): String = {
    val url = params.get("onSuccess") match {
      case Some(onSuccessRedirectUrl) => casOppijaServiceUrl + "?onSuccess=" + onSuccessRedirectUrl
      case None => casOppijaServiceUrl
    }

    val attrs: Either[Throwable, OppijaAttributes] = casOppijaClient.validateServiceTicket(url)(ticket, casOppijaClient.decodeOppijaAttributes).handleWith {
      case NonFatal(t) => Task.fail(new CasClientException(s"Failed to validate service ticket $t"))
    }.unsafePerformSyncAttemptFor(10000).toEither
    logger.debug(s"attrs response: $attrs")
    attrs match {
      case Right(attrs) => {
        val hetu = attrs("nationalIdentificationNumber")
        hetu
      }
      case Left(t) => {
        throw new CasClientException(s"Unable to process CAS Oppija login request, hetu cannot be resolved from ticket $ticket")
      }
    }
  }

  def validateVirkailijaServiceTicket(ticket: String): Username = {
    mockUsernameForAllVirkailijaTickets.getOrElse({
      val attrs: Either[Throwable, Username] = casVirkailijaClient.validateServiceTicket(casVirkailijaServiceUrl)(ticket, casVirkailijaClient.decodeVirkailijaUsername).handleWith {
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
    })
  }
}
