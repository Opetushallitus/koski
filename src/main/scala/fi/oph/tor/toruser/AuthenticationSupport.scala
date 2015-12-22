package fi.oph.tor.toruser

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import fi.oph.tor.json.Json
import fi.vm.sade.security.ldap.DirectoryClient
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraBase
import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.servlet.RichRequest

trait AuthenticationSupport extends ScentrySupport[AuthenticationUser] with BasicAuthSupport[AuthenticationUser] { self: ScalatraBase =>
  val realm = "Todennetun Osaamisen Rekisteri"

  def directoryClient: DirectoryClient

  protected def fromSession = { case user: String => Json.read[AuthenticationUser](user)  }
  protected def toSession   = { case user: AuthenticationUser => Json.write(user) }

  protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]

  override protected def configureScentry = {
    scentry.unauthenticated {
      // When user authenticated user is need and not found, we send 401. Don't send a Basic Auth challenge, because browser will intercept that and show popup.
      scentry.strategies("UsernamePassword").unauthenticated()
    }
  }

  override protected def registerAuthStrategies = {
    scentry.register("UsernamePassword", app => new UserPasswordStrategy(app, directoryClient))
    scentry.register("Basic", app => new TorBasicAuthStrategy(app, realm, directoryClient))
  }
}

class TorBasicAuthStrategy(protected override val app: ScalatraBase, realm: String, val directoryClient: DirectoryClient) extends BasicAuthStrategy[AuthenticationUser](app, realm) with TorAuthenticationStrategy with Logging {
  override protected def getUserId(user: AuthenticationUser)(implicit request: HttpServletRequest, response: HttpServletResponse): String = user.oid

  override protected def validate(username: String, password: String)(implicit request: HttpServletRequest, response: HttpServletResponse): Option[AuthenticationUser] = {
    tryLogin(username, password)
  }
}

trait TorAuthenticationStrategy extends Logging {
  def directoryClient: DirectoryClient
  def tryLogin(username: String, password: String) = {
    val loginResult: Boolean = directoryClient.authenticate(username, password)

    if(!loginResult) {
      None
    } else {
      directoryClient.findUser(username).map { ldapUser =>
        AuthenticationUser(ldapUser.oid, ldapUser.givenNames + " " + ldapUser.lastName)
      } match {
        case Some(user) =>
          Some(user)
        case _ =>
          logger.error("User " + username + " not found from LDAP")
          None
      }
    }
  }
}

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy

class UserPasswordStrategy(protected val app: ScalatraBase, val directoryClient: DirectoryClient)(implicit request: HttpServletRequest, response: HttpServletResponse) extends ScentryStrategy[AuthenticationUser] with TorAuthenticationStrategy with Logging {
  override def name: String = "UserPassword"

  private def loginRequestInBody = {
    try {
      Some(Json.read[Login](RichRequest(request).body))
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
      case Some(Login(username, password)) => tryLogin(username, password)
      case _ => None
    }
  }

  /**
   * What should happen if the user is currently not authenticated
   */
  override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
    app.halt(401, "Not authenticated")
  }
}