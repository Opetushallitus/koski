package fi.oph.koski.koskiuser

import java.util.UUID

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log._
import fi.oph.koski.servlet.KoskiSpecificBaseServlet
import fi.oph.koski.sso.SSOSupport
import fi.oph.koski.userdirectory.Password
import org.scalatra.auth.strategy.BasicAuthStrategy

trait AuthenticationSupport extends KoskiSpecificBaseServlet with SSOSupport {
  val realm = "Koski"

  def application: UserAuthenticationContext

  def haltWithStatus(status: HttpStatus)

  def setUser(user: Either[HttpStatus, AuthenticationUser]): Either[HttpStatus, AuthenticationUser] = {
    request.setAttribute("authUser", user)
    user.right.toOption.filter(_.serviceTicket.isDefined).foreach { user =>
      if (user.kansalainen) {
        setKansalaisCookie(user.copy(huollettavat = None))
      } else {
        setUserCookie(user)
      }
    }
    user
  }

  def getUser: Either[HttpStatus, AuthenticationUser] = {
    Option(request.getAttribute("authUser").asInstanceOf[Either[HttpStatus, AuthenticationUser]]) match {
      case Some(user) => user
      case _ =>
        val authUser: Either[HttpStatus, AuthenticationUser] = userFromCookie match {
          case Right(user) => Right(user)
          case Left(SessionStatusExpiredKansalainen) => Left(KoskiErrorCategory.unauthorized.notAuthenticated())
          case Left(_) => userFromBasicAuth
        }
        setUser(authUser)
        authUser
    }
  }


  private def userFromCookie: Either[SessionStatus, AuthenticationUser] = {
    def getUser(authUser: Option[AuthenticationUser]): Either[SessionStatus, AuthenticationUser] =
      authUser.flatMap(_.serviceTicket).map(ticket => (ticket, application.koskiSessionRepository.getUserByTicket(ticket))) match {
        case Some((_, Some(usr))) => Right(usr)
        case Some((ticket, None)) =>
          removeUserCookie
          setUser(Left(KoskiErrorCategory.unauthorized.notAuthenticated())) // <- to prevent getLogger call from causing recursive calls here
          defaultLogger.warn("User not found by ticket " + ticket)
          if (authUser.exists(_.kansalainen)) Left(SessionStatusExpiredKansalainen) else Left(SessionStatusExpiredVirkailija)
        case _ => Left(SessionStatusNoSession)
      }

    getUser(getUserCookie) match {
      case Right(user) => Right(user)
      case Left(SessionStatusNoSession) => getUser(getKansalaisCookie).map(u => u.copy(kansalainen = true))
      case Left(sessionStatus) => Left(sessionStatus)
    }
  }

  private def userFromBasicAuth: Either[HttpStatus, AuthenticationUser] = {
    val basicAuthRequest = new BasicAuthStrategy.BasicAuthRequest(request)
    if (basicAuthRequest.isBasicAuth && basicAuthRequest.providesAuth) {
      tryLogin(basicAuthRequest.username, basicAuthRequest.password)
    } else {
      Left(KoskiErrorCategory.unauthorized.notAuthenticated())
    }
  }

  def isAuthenticated = getUser.isRight

  def sessionOrStatus: Either[SessionStatus, KoskiSpecificSession] =
    userFromCookie.map(createSession)

  override def koskiSessionOption: Option[KoskiSpecificSession] =
    getUser.toOption.map(createSession)

  private val loginFail = Left(KoskiErrorCategory.unauthorized.loginFail(s"Sisäänkirjautuminen epäonnistui, väärä käyttäjätunnus tai salasana."))

  def tryLogin(username: String, password: String): Either[HttpStatus, AuthenticationUser] = {
    val loginResult: Boolean = application.directoryClient.authenticate(username, Password(password))
    if (!loginResult) {
      logger(LogUserContext(request)).info(s"Login failed with username ${username}")
      loginFail
    } else {
      DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
        case Some(user) =>
          Right(user)
        case None =>
          defaultLogger.warn(s"User not found, after successful authentication: $username")
          loginFail
      }
    }
  }

  def requireVirkailijaOrPalvelukäyttäjä = {
    getUser match {
      case Right(user) if user.kansalainen =>
        haltWithStatus(KoskiErrorCategory.forbidden.vainVirkailija())
      case Right(user) =>
        val session = createSession(user)
        if (session.hasLuovutuspalveluAccess || session.hasTilastokeskusAccess || session.hasKelaAccess) {
          haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
        }
      case Left(error) =>
        haltWithStatus(error)
    }
  }

  def requireKansalainen = {
    getUser match {
      case Right(user) if !user.kansalainen => haltWithStatus(KoskiErrorCategory.forbidden.vainKansalainen())
      case Right(user) =>
      case Left(error) => haltWithStatus(error)
    }
  }

  def requireSession = getUser match {
    case Left(error) => haltWithStatus(error)
    case _ =>
  }

  def localLogin(user: AuthenticationUser, lang: Option[String] = None): AuthenticationUser = {
    val fakeServiceTicket: String = "koski-" + UUID.randomUUID()
    application.koskiSessionRepository.store(fakeServiceTicket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
    logger.info("Local session ticket created: " + fakeServiceTicket)
    UserLanguage.setLanguageCookie(lang.getOrElse(UserLanguage.getLanguageFromLDAP(user, application.directoryClient)), response)
    user.copy(serviceTicket = Some(fakeServiceTicket))
  }

  def createSession(user: AuthenticationUser) = KoskiSpecificSession(user, request, application.käyttöoikeusRepository)
}
