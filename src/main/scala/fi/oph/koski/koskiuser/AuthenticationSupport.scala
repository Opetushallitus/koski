package fi.oph.koski.koskiuser

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log._
import fi.oph.koski.servlet.KoskiBaseServlet
import fi.oph.koski.sso.SSOSupport
import fi.vm.sade.security.ldap.DirectoryClient
import org.scalatra.auth.strategy.BasicAuthStrategy

trait AuthenticationSupport extends KoskiBaseServlet with SSOSupport with Logging {
  val realm = "Koski"

  def application: UserAuthenticationContext

  def haltWithStatus(status: HttpStatus)

  def setUser(user: Either[HttpStatus, AuthenticationUser]) = {
    request.setAttribute("authUser", user)
    user.right.toOption.filter(_.serviceTicket.isDefined).foreach { user =>
      setUserCookie(user)
    }
    user
  }

  def getUser: Either[HttpStatus, AuthenticationUser] = {
    Option(request.getAttribute("authUser").asInstanceOf[Either[HttpStatus, AuthenticationUser]]) match {
      case Some(user) => user
      case _ =>
        def userFromCookie = getUserCookie.flatMap { authUser =>
          authUser.serviceTicket.flatMap { ticket =>
            application.koskiSessionRepository.getUserByTicket(ticket) match {
              case Some(user) =>
                Some(user)
              case None =>
                setUser(Left(KoskiErrorCategory.unauthorized.notAuthenticated())) // <- to prevent getLogger call from causing recursive calls here
                logger.warn("User not found by ticket " + ticket)
                None
            }
          }
        }
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

  def isAuthenticated = getUser.isRight

  override def koskiSessionOption: Option[KoskiSession] = {
    getUser.right.toOption.map { user: AuthenticationUser =>
      KoskiSession(user, lang, request, application.käyttöoikeusRepository)
    }
  }

  def tryLogin(username: String, password: String): Either[HttpStatus, AuthenticationUser] = {
    val attemptAllowed = application.basicAuthSecurity.attemptAllowed(username)

    if (!attemptAllowed) {
      logger(LogUserContext(request)).warn(s"Login blocked for username ${username}")
      return Left(KoskiErrorCategory.unauthorized.loginFail())
    }

    val loginResult: Boolean = application.directoryClient.authenticate(username, password)

    val result = if (!loginResult) {
      logger(LogUserContext(request)).info(s"Login failed with username ${username}")
      Left(KoskiErrorCategory.unauthorized.loginFail(s"Sisäänkirjautuminen käyttäjätunnuksella $username epäonnistui."))
    } else {
      DirectoryClientLogin.findUser(application.directoryClient, request, username) match {
        case Some(user) =>
          Right(user)
        case None =>
          logger.error("User not found from LDAP after successful authentication: " + username)
          Left(KoskiErrorCategory.unauthorized.loginFail())
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
}

object DirectoryClientLogin extends Logging {
  def findUser(directoryClient: DirectoryClient, request: HttpServletRequest, username: String): Option[AuthenticationUser] = {
    directoryClient.findUser(username).map { ldapUser =>
      AuthenticationUser.fromLdapUser(username, ldapUser)
    } match {
      case Some(user) =>
        logger(LogUserContext(request, user.oid, username)).debug("Login successful")
        Some(user)
      case _ =>
        logger(LogUserContext(request)).error("User " + username + " not found from LDAP")
        None
    }
  }
}

