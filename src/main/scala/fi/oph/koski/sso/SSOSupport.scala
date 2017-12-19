package fi.oph.koski.sso

import java.net.{URL, URLDecoder, URLEncoder}

import com.typesafe.config.Config
import fi.oph.koski.json.JsonSerializer
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

  private def currentUrl =
    new URL(koskiRoot + request.getServletPath + request.getPathInfo).toURI.toASCIIString


  private def removeCookie(name: String) = response.addCookie(Cookie(name, "")(CookieOptions(secure = isHttps, path = "/", maxAge = 0, httpOnly = true)))

  def setUserCookie(user: AuthenticationUser) = {
    setCookie("koskiUser", user)
    removeCookie("koskiOppija")
  }

  def setKansalaisCookie(user: AuthenticationUser) = {
    setCookie("koskiOppija", user, domain = cookieDomain)
  }

  private def setCookie(name: String, user: AuthenticationUser, domain: String = "") =
    response.addCookie(Cookie(name, URLEncoder.encode(JsonSerializer.writeWithRoot(user), "UTF-8"))(CookieOptions(domain = domain, secure = isHttps, path = "/", maxAge = application.sessionTimeout.seconds, httpOnly = true)))

  def getUserCookie: Option[AuthenticationUser] = getAuthCookie("koskiUser")
  def getKansalaisCookie: Option[AuthenticationUser] = getAuthCookie("koskiOppija")

  def getAuthCookie(name: String): Option[AuthenticationUser] = {
    val cookie = Option(request.getCookies).toList.flatten.find(_.getName == name)
    cookie.map(_.getValue).map(c => URLDecoder.decode(c, "UTF-8")).flatMap( json =>
      try {
        Some(JsonSerializer.parse[AuthenticationUser](json))
      } catch {
        case e: Exception =>
          removeUserCookie
          defaultLogger.warn(e)("Error decoding authentication cookie")
          None
      }
    )
  }

  def removeUserCookie = List("koskiOppija", "koskiUser").foreach(removeCookie)

  def casServiceUrl = {
    koskiRoot + "/cas"
  }

  def redirectAfterLogin = {
    val returnUrlCookie = Option(request.getCookies).toList.flatten.find(_.getName == "koskiReturnUrl").map(_.getValue)
    removeCookie("koskiReturnUrl")
    redirect(returnUrlCookie.getOrElse("/"))
  }

  def redirectToLogin = {
    response.addCookie(Cookie("koskiReturnUrl", currentUrl)(CookieOptions(secure = isHttps, path = "/", maxAge = 60, httpOnly = true)))
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

  def redirectToFrontpage = redirect("/")

  def ssoConfig = SSOConfig(application.config)

  def localLoginPage = "/login"

  private def cookieDomain = application.config.getString("koski.cookieDomain")
}

case class SSOConfig(config: Config) {
  def isCasSsoUsed = config.getString("opintopolku.virkailija.url") != "mock"
}