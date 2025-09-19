package fi.oph.koski.koskiuser

import java.util.UUID
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer.writeWithRoot
import fi.oph.koski.log._
import fi.oph.koski.servlet.BaseServlet
import fi.oph.koski.sso.{KoskiUserCookie, SSOSupport}
import fi.oph.koski.userdirectory.Password
import org.scalatra.{Cookie, CookieOptions}
import org.scalatra.auth.strategy.BasicAuthStrategy

import java.net.URLEncoder.encode

trait AuthenticationSupport extends BaseServlet with SSOSupport {
  def application: UserAuthenticationContext

  def haltWithStatus(status: HttpStatus)

  def setUser(user: Either[HttpStatus, AuthenticationUser]): Either[HttpStatus, AuthenticationUser] = {
    request.setAttribute("authUser", user)
    user.right.toOption.filter(_.serviceTicket.isDefined).foreach { user =>
      val cookie = KoskiUserCookie(serviceTicket = user.serviceTicket.get, kansalainen = user.kansalainen)
      if (user.kansalainen) {
        setKansalaisCookie(cookie)
      } else {
        setUserCookie(cookie)
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
    def getUser(cookie: Option[KoskiUserCookie]): Either[SessionStatus, AuthenticationUser] =
      cookie.map(_.serviceTicket).map(ticket => (ticket, application.koskiSessionRepository.getUserByTicket(ticket))) match {
        case Some((_, Some(usr))) => Right(usr)
        case Some((ticket, None)) =>
          removeUserCookie
          setUser(Left(KoskiErrorCategory.unauthorized.notAuthenticated())) // <- to prevent getLogger call from causing recursive calls here
          defaultLogger.warn("User not found by ticket " + ticket)
          if (cookie.exists(_.kansalainen)) Left(SessionStatusExpiredKansalainen) else Left(SessionStatusExpiredVirkailija)
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

  def sessionOrStatus: Either[SessionStatus, Session] =
    userFromCookie.map(createSession)

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
    UserLanguage.setLanguageCookie(lang.getOrElse(UserLanguage.getLanguageFromLDAP(user, application.directoryClient).getOrElse("fi")), response)
    user.copy(serviceTicket = Some(fakeServiceTicket))
  }

  def createSession(user: AuthenticationUser): Session

  def setCookie(cookieName: String, value: String): Unit = {
    response.addCookie(Cookie(cookieName, encode(writeWithRoot(value), "UTF-8"))(
      CookieOptions(
        secure = isHttps,
        path = "/",
        maxAge = application.sessionTimeout.seconds,
        httpOnly = true
      )
    ))
  }
}
