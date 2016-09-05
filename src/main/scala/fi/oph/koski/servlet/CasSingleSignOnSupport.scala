package fi.oph.koski.servlet

import java.net.{URLDecoder, URLEncoder, URI}

import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AuthenticationUser, UserAuthenticationContext}
import org.scalatra.{Cookie, CookieOptions, ScalatraBase}

trait CasSingleSignOnSupport extends ScalatraBase {
  def application: UserAuthenticationContext

  private def currentUrl = request.getRequestURL.toString

  def isHttps = !currentUrl.startsWith("http://localhost") // <- we don't get the https protocol correctly through the proxy, so we assume https

  def setUserCookie(user: AuthenticationUser) = {
    response.addCookie(Cookie("koskiUser", URLEncoder.encode(Json.write(user), "UTF-8"))(CookieOptions(secure = isHttps, path = "/", maxAge = application.sessionTimeout.seconds, httpOnly = true)))
  }
  def getUserCookie: Option[AuthenticationUser] = {
    Option(request.getCookies).toList.flatten.find(_.getName == "koskiUser").map(_.getValue).map(c => URLDecoder.decode(c, "UTF-8")).map(Json.read[AuthenticationUser])
  }
  def removeUserCookie = response.addCookie(Cookie("koskiUser", "")(CookieOptions(secure = isHttps, path = "/", maxAge = 0)))

  def casServiceUrl = {
    def fixProtocol(url: String) = if (!isHttps) {
      url
    } else {
      url.replace("http://", "https://")// <- we don't get the https protocol correctly through the proxy, so we replace it manually
    }
    fixProtocol {
      val subpath = request.getServletPath + request.pathInfo
      (if (currentUrl.endsWith(subpath)) {
        currentUrl.substring(0, currentUrl.length - subpath.length)
      } else {
        currentUrl
      }) + "/cas"
    }
  }

  def redirectAfterLogin = {
    val returnUrlCookie = Option(request.getCookies).toList.flatten.find(_.getName == "koskiReturnUrl").map(_.getValue)
    response.addCookie(Cookie("koskiReturnUrl", "")(CookieOptions(secure = isHttps, path = "/", maxAge = 0)))
    redirect(returnUrlCookie.getOrElse("/"))
  }

  def redirectToLogin = {
    response.addCookie(Cookie("koskiReturnUrl", currentUrl)(CookieOptions(secure = isHttps, path = "/", maxAge = 60)))
    if (isCasSsoUsed) {
      redirect(application.config.getString("opintopolku.virkailija.url") + "/cas/login?service=" + casServiceUrl)
    } else {
      redirect("/")
    }
  }

  def redirectToLogout = {
    if (isCasSsoUsed) {
      val koskiRoot = URI.create(request.getRequestURL().toString()).resolve(request.getContextPath())
      redirect(application.config.getString("opintopolku.virkailija.url") + "/cas/logout?service=" + koskiRoot)
    } else {
      redirect("/")
    }
  }

  def isCasSsoUsed = application.config.hasPath("opintopolku.virkailija.url")
}
