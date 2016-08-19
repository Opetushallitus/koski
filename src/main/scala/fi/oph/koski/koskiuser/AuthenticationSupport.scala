package fi.oph.koski.koskiuser

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log._
import fi.oph.koski.servlet.CasSingleSignOnSupport
import fi.vm.sade.security.ldap.DirectoryClient
import org.scalatra.ScalatraServlet
import org.scalatra.auth.strategy.BasicAuthStrategy

trait AuthenticationSupport extends ScalatraServlet with CasSingleSignOnSupport with Logging {
  val realm = "Koski"

  def application: UserAuthenticationContext


  def haltWithStatus(status: HttpStatus)

  def userNotAuthenticatedError = {
    haltWithStatus(KoskiErrorCategory.unauthorized())
  }

  def setUser(user: AuthenticationUser) = {
    request.setAttribute("authUser", user)
    if (user.serviceTicket.isDefined)
      setUserCookie(user)
  }

  def userOption = {
    Option(request.getAttribute("authUser").asInstanceOf[AuthenticationUser]).orElse{
      def userFromCookie = getUserCookie.flatMap { authUser =>
        authUser.serviceTicket.flatMap { ticket =>
          application.serviceTicketRepository.getUserByTicket(ticket) match {
            case Some(user) =>
              Some(user)
            case None =>
              logger.warn("User not found by ticket " + ticket)
              None
          }
        }
      }
      def userFromBasicAuth = {
        implicit def request2BasicAuthRequest(r: HttpServletRequest) = new BasicAuthStrategy.BasicAuthRequest(r)
        if (request.isBasicAuth && request.providesAuth) {
          tryLogin(request.username, request.password)
        } else {
          None
        }
      }
      val authUser = userFromCookie.orElse(userFromBasicAuth)
      authUser.foreach(setUser)
      authUser
    }
  }

  def isAuthenticated = userOption.isDefined

  def koskiUserOption: Option[KoskiUser] = {
    def toKoskiUser(user: AuthenticationUser) = KoskiUser(user.oid, request, application.käyttöoikeusRepository)
    userOption.map(toKoskiUser)
  }

  def tryLogin(username: String, password: String): Option[AuthenticationUser] = {
    val loginResult: Boolean = application.directoryClient.authenticate(username, password)

    if(!loginResult) {
      logger(LogUserContext(request)).info(s"Login failed with username ${username}")
      None
    } else {
      DirectoryClientLogin.findUser(application.directoryClient, request, username)
    }
  }

  def requireAuthentication = {
    userOption match {
      case Some(user) =>
      case None => userNotAuthenticatedError
    }
  }
}

object DirectoryClientLogin extends Logging {
  def findUser(directoryClient: DirectoryClient, request: HttpServletRequest, username: String): Option[AuthenticationUser] = {
    directoryClient.findUser(username).map { ldapUser =>
      AuthenticationUser(ldapUser.oid, ldapUser.givenNames + " " + ldapUser.lastName, None)
    } match {
      case Some(user) =>
        logger(LogUserContext(request, user.oid)).info("Login successful")
        Some(user)
      case _ =>
        logger(LogUserContext(request)).error("User " + username + " not found from LDAP")
        None
    }
  }
}