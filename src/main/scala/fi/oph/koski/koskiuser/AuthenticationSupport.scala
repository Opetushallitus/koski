package fi.oph.koski.koskiuser

import java.util.UUID
import javax.servlet.http.HttpServletRequest

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log._
import fi.oph.koski.servlet.KoskiBaseServlet
import fi.oph.koski.sso.SSOSupport
import fi.oph.koski.userdirectory.DirectoryClient
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
          implicit def request2BasicAuthRequest(r: HttpServletRequest) = new BasicAuthStrategy.BasicAuthRequest(r)
          if (request.isBasicAuth && request.providesAuth) {
            tryLogin(request.username, request.password)
          } else {
            Left(KoskiErrorCategory.unauthorized.notAuthenticated())
          }
        }

        val authUser: Either[HttpStatus, AuthenticationUser] = userFromCookie match {
          case Some(user) => Right(user)
          case None => userFromBasicAuth
        }
        setUser(authUser)
        authUser
    }
  }


  private def userFromCookie: Option[AuthenticationUser] = {
    def getUser(authUser: Option[AuthenticationUser]): Option[AuthenticationUser] =
      authUser.flatMap(_.serviceTicket).map(ticket => (ticket, application.koskiSessionRepository.getUserByTicket(ticket))) match {
        case Some((_, Some(usr))) => Some(usr)
        case Some((ticket, None)) =>
          removeUserCookie
          setUser(Left(KoskiErrorCategory.unauthorized.notAuthenticated())) // <- to prevent getLogger call from causing recursive calls here
          logger.warn("User not found by ticket " + ticket)
          None
        case noTicket => None
      }

    getUser(getUserCookie).orElse(getUser(getKansalaisCookie).map(u => u.copy(kansalainen = true)))
  }

  def isAuthenticated = getUser.isRight

  override def koskiSessionOption: Option[KoskiSession] = {
    getUser.right.toOption.map { user: AuthenticationUser =>
      KoskiSession(user, request, application.käyttöoikeusRepository)
    }
  }

  def tryLogin(username: String, password: String): Either[HttpStatus, AuthenticationUser] = {
    // prevent brute-force login by blocking incorrect logins with progressive delay
    lazy val loginFail = Left(KoskiErrorCategory.unauthorized.loginFail(s"Sisäänkirjautuminen käyttäjätunnuksella $username epäonnistui."))
    val blockedUntil = application.basicAuthSecurity.getLoginBlocked(username)
    if (blockedUntil.isDefined) {
      logger(LogUserContext(request)).warn(s"Too many failed login attempts for username ${username}, blocking login until ${blockedUntil.get}")
      return loginFail
    }

    val loginResult: Boolean = application.directoryClient.authenticate(username, password)
    val result = if (!loginResult) {
      logger(LogUserContext(request)).info(s"Login failed with username ${username}")
      loginFail
    } else {
      DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
        case Some(user) =>
          Right(user)
        case None =>
          logger.error(s"User not found, after successful authentication: $username")
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

  def requireAuthentication = {
    getUser match {
      case Right(user) =>
      case Left(error) => haltWithStatus(error)
    }
  }

  def localLogin(user: AuthenticationUser): AuthenticationUser = {
    val fakeServiceTicket: String = "koski-" + UUID.randomUUID()
    application.koskiSessionRepository.store(fakeServiceTicket, user, LogUserContext.clientIpFromRequest(request))
    logger.info("Local session ticket created: " + fakeServiceTicket)
    KoskiUserLanguage.setLanguageCookie(KoskiUserLanguage.getLanguageFromLDAP(user, application.directoryClient), response)
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
        logger(LogUserContext(request)).error(s"User $username not found")
        None
    }
  }
}

