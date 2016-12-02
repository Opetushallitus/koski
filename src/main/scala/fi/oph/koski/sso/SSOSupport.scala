package fi.oph.koski.sso

import java.net.{URLDecoder, URLEncoder}

import com.typesafe.config.Config
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AuthenticationUser, UserAuthenticationContext}
import fi.oph.koski.log.Logging
import org.scalatra.{Cookie, CookieOptions, ScalatraBase}

trait SSOSupport extends ScalatraBase with Logging {
  def application: UserAuthenticationContext

  def isHttps = {
    request.header("X-Forwarded-For").isDefined || request.isSecure // If we are behind a loadbalancer proxy, we assume that https is used
  }

  def protocol = if (isHttps) { "https" } else { "http" }

  def koskiRoot: String = {
    val hostRegex = "https?://([^/]*)/.*".r
    request.getRequestURL match {
      case hostRegex(host) => protocol + "://" + host + "/koski"
    }
  }

  private def currentUrl = {
    koskiRoot + request.getServletPath + request.getPathInfo
  }

  private def removeCookie(name: String) = response.addCookie(Cookie(name, "")(CookieOptions(secure = isHttps, path = "/", maxAge = 0)))

  def setUserCookie(user: AuthenticationUser) = {
    response.addCookie(Cookie("koskiUser", URLEncoder.encode(Json.write(user), "UTF-8"))(CookieOptions(secure = isHttps, path = "/", maxAge = application.sessionTimeout.seconds, httpOnly = true)))
  }
  def getUserCookie: Option[AuthenticationUser] = {
    Option(request.getCookies).toList.flatten.find(_.getName == "koskiUser").map(_.getValue).map(c => URLDecoder.decode(c, "UTF-8")).flatMap( json =>
      try {
        Some(Json.read[AuthenticationUser](json))
      } catch {
        case e: Exception =>
          removeUserCookie
          defaultLogger.warn(e)("Error decoding authentication cookie")
          None
      }
    )
  }
  def removeUserCookie = removeCookie("koskiUser")

  def casServiceUrl = {
    koskiRoot + "/cas"
  }

  def redirectAfterLogin = {
    val returnUrlCookie = Option(request.getCookies).toList.flatten.find(_.getName == "koskiReturnUrl").map(_.getValue)
    removeCookie("koskiReturnUrl")
    redirect(returnUrlCookie.getOrElse("/"))
  }

  def redirectToLogin = {
    response.addCookie(Cookie("koskiReturnUrl", currentUrl)(CookieOptions(secure = isHttps, path = "/", maxAge = 60)))
    if (ssoConfig.isCasSsoUsed) {
      redirect(application.config.getString("opintopolku.virkailija.url") + "/cas/login?service=" + casServiceUrl)
    } else {
      redirect(localLoginPage)
    }
  }

  def redirectToLogout = {
    if (ssoConfig.isCasSsoUsed) {
      redirect(application.config.getString("opintopolku.virkailija.url") + "/cas/logout?service=" + koskiRoot)
    } else {
      redirect(localLoginPage)
    }
  }

  def ssoConfig = SSOConfig(application.config)

  def localLoginPage = "/login/login.html"
}

case class SSOConfig(config: Config) {
  def isCasSsoUsed = config.hasPath("opintopolku.virkailija.url")
}