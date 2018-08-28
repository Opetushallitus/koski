package fi.oph.koski.koskiuser

import java.util.UUID
import javax.servlet.http.HttpServletRequest

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log._
import fi.oph.koski.servlet.KoskiBaseServlet
import fi.oph.koski.sso.SSOSupport
import fi.oph.koski.userdirectory.{DirectoryClient, Password}
import org.scalatra.auth.strategy.BasicAuthStrategy

trait AuthenticationSupport extends KoskiBaseServlet with SSOSupport with Logging {
  val realm = "Koski"

  def application: UserAuthenticationContext

  def haltWithStatus(status: HttpStatus)

  def setUser(user: Either[HttpStatus, AuthenticationUser]): Either[HttpStatus, AuthenticationUser] = {
    request.setAttribute("authUser", user)
    user.right.toOption.filter(_.serviceTicket.isDefined).foreach { user =>
      if (user.kansalainen) {
        setKansalaisCookie(user)
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
        def userFromBasicAuth: Either[HttpStatus, AuthenticationUser] = {
          val basicAuthRequest = new BasicAuthStrategy.BasicAuthRequest(request)
          if (basicAuthRequest.isBasicAuth && basicAuthRequest.providesAuth) {
            tryLogin(basicAuthRequest.username, basicAuthRequest.password)
          } else {
            Left(KoskiErrorCategory.unauthorized.notAuthenticated())
          }
        }

        val authUser: Either[HttpStatus, AuthenticationUser] = userFromCookie match {
          case Right(user) => Right(user)
          case Left(SessionStatusExpiredKansalainen) => Left(KoskiErrorCategory.unauthorized.notAuthenticated())
          case Left(_) => userFromBasicAuth
        }
        setUser(authUser)
        authUser
    }
  }


  private def userFromCookie: Either[KoskiSessionStatus, AuthenticationUser] = {
    def getUser(authUser: Option[AuthenticationUser]): Either[KoskiSessionStatus, AuthenticationUser] =
      authUser.flatMap(_.serviceTicket).map(ticket => (ticket, application.koskiSessionRepository.getUserByTicket(ticket))) match {
        case Some((_, Some(usr))) => Right(usr)
        case Some((ticket, None)) =>
          removeUserCookie
          setUser(Left(KoskiErrorCategory.unauthorized.notAuthenticated())) // <- to prevent getLogger call from causing recursive calls here
          logger.warn("User not found by ticket " + ticket)
          if (authUser.exists(_.kansalainen)) Left(SessionStatusExpiredKansalainen) else Left(SessionStatusExpiredVirkailija)
        case _ => Left(SessionStatusNoSession)
      }

    getUser(getUserCookie) match {
      case Right(user) => Right(user)
      case Left(SessionStatusNoSession) => getUser(getKansalaisCookie).map(u => u.copy(kansalainen = true))
      case Left(sessionStatus) => Left(sessionStatus)
    }
  }

  def isAuthenticated = getUser.isRight

  def sessionOrStatus: Either[KoskiSessionStatus, KoskiSession] = {
    userFromCookie.map { user: AuthenticationUser =>
      KoskiSession(user, request, application.käyttöoikeusRepository)
    }
  }

  override def koskiSessionOption: Option[KoskiSession] = {
    getUser.right.toOption.map { user: AuthenticationUser =>
      KoskiSession(user, request, application.käyttöoikeusRepository)
    }
  }

  def tryLogin(username: String, password: String): Either[HttpStatus, AuthenticationUser] = {
    // prevent brute-force login by blocking incorrect logins with progressive delay
    lazy val loginFail = Left(KoskiErrorCategory.unauthorized.loginFail(s"Sisäänkirjautuminen epäonnistui, väärä käyttäjätunnus tai salasana."))
    val blockedUntil = application.basicAuthSecurity.getLoginBlocked(username)
    if (blockedUntil.isDefined) {
      logger(LogUserContext(request)).warn(s"Too many failed login attempts for username ${username}, blocking login until ${blockedUntil.get}")
      return loginFail
    }

    val loginResult: Boolean = application.directoryClient.authenticate(username, Password(password))
    val result = if (!loginResult) {
      logger(LogUserContext(request)).info(s"Login failed with username ${username}")
      loginFail
    } else {
      DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
        case Some(user) =>
          Right(user)
        case None =>
          logger.warn(s"User not found, after successful authentication: $username")
          loginFail
      }
    }

    if (result.isLeft) {
      application.basicAuthSecurity.loginFailed(username)
    } else {
      application.basicAuthSecurity.loginSuccess(username)
    }
    result
  }

  def requireVirkailijaOrPalvelukäyttäjä = {
    getUser match {
      case Right(user) if user.kansalainen => haltWithStatus(KoskiErrorCategory.forbidden.vainVirkailija())
      case Right(user) =>
      case Left(error) => haltWithStatus(error)
    }
  }

  def requireKansalainen = {
    getUser match {
      case Right(user) if !user.kansalainen => haltWithStatus(KoskiErrorCategory.forbidden.vainKansalainen())
      case Right(user) =>
      case Left(error) => haltWithStatus(error)
    }
  }

  def localLogin(user: AuthenticationUser, lang: Option[String] = None): AuthenticationUser = {
    val fakeServiceTicket: String = "koski-" + UUID.randomUUID()
    application.koskiSessionRepository.store(fakeServiceTicket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
    logger.info("Local session ticket created: " + fakeServiceTicket)
    KoskiUserLanguage.setLanguageCookie(lang.getOrElse(KoskiUserLanguage.getLanguageFromLDAP(user, application.directoryClient)), response)
    user.copy(serviceTicket = Some(fakeServiceTicket))
  }
}

object DirectoryClientLogin extends Logging {
  def findUser(directoryClient: DirectoryClient, request: HttpServletRequest, username: String): Option[AuthenticationUser] = {
    directoryClient.findUser(username).map { ldapUser =>
      AuthenticationUser.fromDirectoryUser(username, ldapUser)
    } match {
      case Some(user) =>
        logger(LogUserContext(request, user.oid, username)).debug("Login successful")
        Some(user)
      case _ =>
        logger(LogUserContext(request)).warn(s"User $username not found")
        None
    }
  }
}

