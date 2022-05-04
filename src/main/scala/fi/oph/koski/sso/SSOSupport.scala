package fi.oph.koski.sso

import java.net.{URI, URLDecoder, URLEncoder}
import com.typesafe.config.Config
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AuthenticationUser, UserAuthenticationContext}
import fi.oph.koski.log.Logging
import org.scalatra.{Cookie, CookieOptions, ScalatraBase}

import scala.collection.JavaConverters._

trait SSOSupport extends ScalatraBase with Logging {
  def application: UserAuthenticationContext

  def isHttps = {
    request.header("X-Forwarded-For").isDefined || request.isSecure // If we are behind a loadbalancer proxy, we assume that https is used
  }

  def protocol = if (isHttps) { "https" } else { "http" }

  def serviceRoot: String

  private def currentUrl: String = try {
    new URI(serviceRoot + request.getServletPath + request.getPathInfo).toASCIIString
  } catch {
    case e: Exception =>
      logger.warn(s"Problem parsing url: ${e.getMessage}")
      serviceRoot + request.getServletPath
  }


  private def removeCookie(name: String, domain: String = "") = response.addCookie(Cookie(name, "")(CookieOptions(domain = domain, secure = isHttps, path = "/", maxAge = 0, httpOnly = true)))

  def setUserCookie(user: AuthenticationUser) = {
    setCookie("koskiUser", user)
    removeCookie("koskiOppija")
  }

  def setKansalaisCookie(user: AuthenticationUser) = {
    cookieDomains.foreach(cookieDomain => setCookie("koskiOppija", user, domain = cookieDomain))
  }

  private def setCookie(name: String, user: AuthenticationUser, domain: String = "") =
    response.addCookie(Cookie(name, URLEncoder.encode(JsonSerializer.writeWithRoot(user), "UTF-8"))(CookieOptions(domain = domain, secure = isHttps, path = "/", maxAge = application.sessionTimeout.seconds, httpOnly = true)))

  def getUserCookie: Option[AuthenticationUser] = getAuthCookie("koskiUser")
  def getKansalaisCookie: Option[AuthenticationUser] = getAuthCookie("koskiOppija")

  def getAuthCookie(name: String): Option[AuthenticationUser] = { val cookie = Option(request.getCookies).toList.flatten.find(_.getName == name)
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

  def removeUserCookie = {
    cookieDomains.foreach(cookieDomain => removeCookie("koskiOppija", domain = cookieDomain))
    removeCookie("koskiUser")
    removeCookie("eisuorituksia")
  }

  def casVirkailijaServiceUrl = {
    serviceRoot + "/koski/cas/virkailija"
  }

  def casOppijaServiceUrl = {
    serviceRoot + "/koski/cas/oppija"
  }

  def redirectAfterLogin = {
    val returnUrlCookie = Option(request.getCookies).toList.flatten.find(_.getName == "koskiReturnUrl").map(_.getValue)
    removeCookie("koskiReturnUrl")
    redirect(returnUrlCookie.getOrElse("/"))
  }

  def redirectToVirkailijaLogin = {
    response.addCookie(Cookie("koskiReturnUrl", currentUrl)(CookieOptions(secure = isHttps, path = "/", maxAge = 60, httpOnly = true)))
    if (ssoConfig.isCasSsoUsed) {
      redirect(application.config.getString("opintopolku.virkailija.url") + "/cas/login?service=" + casVirkailijaServiceUrl)
    } else {
      redirect(localLoginPage)
    }
  }

  def redirectToOppijaLogin = {
    response.addCookie(Cookie("koskiReturnUrl", currentUrl)(CookieOptions(secure = isHttps, path = "/", maxAge = 60, httpOnly = true)))
    if (ssoConfig.isCasSsoUsed) {
      redirect(application.config.getString("opintopolku.oppija.url") + "/cas-oppija/login?service=" + casOppijaServiceUrl + "&valtuudet=false")
    } else {
      redirect(localOppijaLoginPage + "?onSuccess=" + URLEncoder.encode(params.getOrElse("redirect", ""), "UTF-8"))
    }
  }

  def redirectToVirkailijaLogout = {
    if (ssoConfig.isCasSsoUsed) {
      redirect(application.config.getString("opintopolku.virkailija.url") + "/cas/logout?service=" + serviceRoot + "/koski/virkailija")
    } else {
      redirect(localLoginPage)
    }
  }

  def redirectToOppijaLogout(redirectTarget: String = serviceRoot) = {
    if (ssoConfig.isCasSsoUsed) {
      redirect(SSOConfigurationOverride.getValue(application.config, "opintopolku.oppija.url") + "/cas-oppija/logout?service=" + redirectTarget)
    } else {
      redirect(redirectTarget)
    }
  }

  def redirectToFrontpage = redirect("/")

  def ssoConfig = SSOConfig(application.config)

  def localLoginPage: String
  def localOppijaLoginPage: String

  // don't set cookie domain for localhost (so that local Koski works with non-localhost IP address, e.g. phone in the same wifi)
  private def cookieDomains: Iterable[String] =
    application.config.getStringList("koski.cookieDomains").asScala.map { d =>
      if (d == "localhost") ""
      else d
    }
}

case class SSOConfig(config: Config) {
  def isCasSsoUsed = SSOConfigurationOverride.getValue(config, "login.security") != "mock"
}

object SSOConfigurationOverride {
  var overrides: Map[String, String] = Map.empty

  def overrideKey(key: String, value: String): Unit = {
    overrides = overrides + (key -> value)
  }

  def clearOverrides = {
    overrides = Map.empty
  }

  def getValue(config: Config, key: String): String = {
    overrides.get(key).getOrElse(config.getString(key))
  }
}

