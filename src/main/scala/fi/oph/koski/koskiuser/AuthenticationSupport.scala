package fi.oph.koski.koskiuser

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.log._
import fi.oph.koski.servlet.KoskiBaseServlet
import fi.vm.sade.security.ldap.DirectoryClient
import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.servlet.RichRequest

trait AuthenticationSupport extends KoskiBaseServlet with ScentrySupport[AuthenticationUser] with BasicAuthSupport[AuthenticationUser] {
  val realm = "Koski"

  def directoryClient: DirectoryClient
  def userRepository: UserOrganisationsRepository

  protected def fromSession = { case user: String => Json.read[AuthenticationUser](user)  }
  protected def toSession   = { case user: AuthenticationUser => Json.write(user) }

  protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]

  def userNotAuthenticatedError = {
    haltWithStatus(KoskiErrorCategory.unauthorized())
  }

  override def torUserOption: Option[KoskiUser] = {
    userOption.map { authUser =>
      KoskiUser(authUser.oid, request, userRepository)
    }
  }

  override protected def configureScentry = {
    scentry.unauthenticated {
      // When user authenticated user is need and not found, we send 401. Don't send a Basic Auth challenge, because browser will intercept that and show popup.
      scentry.strategies("UsernamePassword").unauthenticated()
    }
  }

  override protected def registerAuthStrategies = {
    scentry.register("UsernamePassword", app => new UserPasswordStrategy(app.asInstanceOf[AuthenticationSupport], directoryClient))
    scentry.register("Basic", app => new KoskiBasicAuthStrategy(app.asInstanceOf[AuthenticationSupport], realm, directoryClient))
  }
}

class KoskiBasicAuthStrategy(protected override val app: AuthenticationSupport, realm: String, val directoryClient: DirectoryClient) extends BasicAuthStrategy[AuthenticationUser](app, realm) with KoskiAuthenticationStrategy with Logging {
  override protected def getUserId(user: AuthenticationUser)(implicit request: HttpServletRequest, response: HttpServletResponse): String = user.oid

  override protected def validate(username: String, password: String)(implicit request: HttpServletRequest, response: HttpServletResponse): Option[AuthenticationUser] = {
    tryLogin(username, password, app)
  }
}

trait KoskiAuthenticationStrategy extends Logging {
  def directoryClient: DirectoryClient
  def tryLogin(username: String, password: String, app: AuthenticationSupport) = {
    val loginResult: Boolean = directoryClient.authenticate(username, password)

    if(!loginResult) {
      logger(LogUserContext(app.request)).info(s"Login failed with username ${username}")
      None
    } else {
      directoryClient.findUser(username).map { ldapUser =>
        AuthenticationUser(ldapUser.oid, ldapUser.givenNames + " " + ldapUser.lastName)
      } match {
        case Some(user) =>
          logger(LogUserContext(app.request, user.oid)).info("Login successful")
          Some(user)
        case _ =>
          logger(LogUserContext(app.request)).error("User " + username + " not found from LDAP")
          None
      }
    }
  }
}

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.scalatra.auth.ScentryStrategy

class UserPasswordStrategy(protected val app: AuthenticationSupport, val directoryClient: DirectoryClient)
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

  /***
    * Determine whether the strategy should be run for the current request.
    */
  override def isValid(implicit request: HttpServletRequest) = {
    loginRequestInBody.isDefined
  }

  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[AuthenticationUser] = {
    loginRequestInBody match {
      case Some(Login(username, password)) => tryLogin(username, password, app)
      case _ => None
    }
  }

  /**
   * What should happen if the user is currently not authenticated
   */
  override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
    app.userNotAuthenticatedError
  }
}