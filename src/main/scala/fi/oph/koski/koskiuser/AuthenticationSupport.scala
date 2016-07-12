package fi.oph.koski.koskiuser

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.log._
import fi.oph.koski.servlet.CasSingleSignOnSupport
import fi.vm.sade.security.ldap.DirectoryClient
import org.scalatra.ScalatraServlet
import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.servlet.RichRequest

trait AuthenticationSupport extends ScalatraServlet with ScentrySupport[AuthenticationUser] with BasicAuthSupport[AuthenticationUser] with CasSingleSignOnSupport with Logging {
  val realm = "Koski"

  def application: UserAuthenticationContext


  protected def fromSession = { case user: String => Json.read[AuthenticationUser](user)  }
  protected def toSession   = { case user: AuthenticationUser => Json.write(user) }

  protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]

  def haltWithStatus(status: HttpStatus)

  def userNotAuthenticatedError = {
    haltWithStatus(KoskiErrorCategory.unauthorized())
  }

  def koskiUserOption: Option[KoskiUser] = {
    def toKoskiUser(user: AuthenticationUser) = KoskiUser(user.oid, request, application.käyttöoikeusRepository)
    userOption.flatMap { authUser =>
      authUser.serviceTicket match {
        case Some(ticket) =>
          application.serviceTicketRepository.getUserByTicket(ticket) map(toKoskiUser)
        case None =>
          Some(toKoskiUser(authUser))
      }
    }
  }

  override protected def configureScentry = {
    scentry.unauthenticated {
      // When user authenticated user is need and not found, we send 401. Don't send a Basic Auth challenge, because browser will intercept that and show popup.
      scentry.strategies("UsernamePassword").unauthenticated()
    }
  }

  override protected def registerAuthStrategies = {
    scentry.register("UsernamePassword", app => new UserPasswordStrategy(app.asInstanceOf[AuthenticationSupport]))
    scentry.register("Basic", app => new KoskiBasicAuthStrategy(app.asInstanceOf[AuthenticationSupport], realm))
    scentry.register("CasServiceTicketCookie", app => new CasServiceTicketCookieStrategy(app.asInstanceOf[AuthenticationSupport]))
  }
}

class KoskiBasicAuthStrategy(protected override val app: AuthenticationSupport, realm: String) extends BasicAuthStrategy[AuthenticationUser](app, realm) with KoskiAuthenticationStrategy with Logging {
  override protected def getUserId(user: AuthenticationUser)(implicit request: HttpServletRequest, response: HttpServletResponse): String = user.oid

  override protected def validate(username: String, password: String)(implicit request: HttpServletRequest, response: HttpServletResponse): Option[AuthenticationUser] = {
    tryLogin(username, password)
  }
}

trait KoskiAuthenticationStrategy extends Logging {
  protected def app: AuthenticationSupport
  def directoryClient: DirectoryClient = app.application.directoryClient

  def tryLogin(username: String, password: String): Option[AuthenticationUser] = {
    val loginResult: Boolean = directoryClient.authenticate(username, password)

    if(!loginResult) {
      logger(LogUserContext(app.request)).info(s"Login failed with username ${username}")
      None
    } else {
      DirectoryClientLogin.findUser(directoryClient, app.request, username)
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

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.scalatra.auth.ScentryStrategy

class UserPasswordStrategy(protected val app: AuthenticationSupport)
                          (implicit request: HttpServletRequest, response: HttpServletResponse)
                           extends ScentryStrategy[AuthenticationUser] with KoskiAuthenticationStrategy with Logging {
  override def name: String = "UserPassword"

  private def loginRequestInBody = {
    try {
      val body: String = RichRequest(request).body
      Some(Json.read[Login](body))
    } catch {
      case e: Exception => None
    }
  }

  override def isValid(implicit request: HttpServletRequest) = {
    loginRequestInBody.isDefined
  }

  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[AuthenticationUser] = {
    loginRequestInBody flatMap {
      case Login(username, password) => tryLogin(username, password)
    }
  }

  override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
    app.userNotAuthenticatedError
  }
}

class CasServiceTicketCookieStrategy(protected val app: AuthenticationSupport)(implicit request: HttpServletRequest, response: HttpServletResponse)
  extends ScentryStrategy[AuthenticationUser] with KoskiAuthenticationStrategy with Logging {


  override def isValid(implicit request: HttpServletRequest) = {
    app.getServiceTicketCookie.isDefined
  }

  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[AuthenticationUser] = {
    app.getServiceTicketCookie flatMap {
      case serviceTicketCookie => app.application.serviceTicketRepository.getUserByTicket(serviceTicketCookie) match {
        case Some(user) =>
          logger.info(s"Restoring session for ${user.name} from service ticket $serviceTicketCookie")
          Some(user)
        case None =>
          app.removeServiceTicketCookie
          None
      }
    }
  }
}

