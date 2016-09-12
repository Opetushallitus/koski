package fi.oph.koski.servlet

import java.net.{URI, URLDecoder, URLEncoder}

import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AuthenticationUser, UserAuthenticationContext}
import fi.oph.koski.log.Debug
import org.scalatra.{Cookie, CookieOptions, ScalatraBase}

trait CasSingleSignOnSupport extends ScalatraBase {
  def application: UserAuthenticationContext

  private val koskiRoot: String = application.config.getString("koski.root.url")

  private def currentUrl = {
    koskiRoot + request.getServletPath + request.getPathInfo
  }

  private def removeCookie(name: String) = response.addCookie(Cookie(name, "")(CookieOptions(secure = isHttps, path = "/", maxAge = 0)))

  def isHttps = {
    koskiRoot.startsWith("https:")
  }

  def setUserCookie(user: AuthenticationUser) = {
    response.addCookie(Cookie("koskiUser", URLEncoder.encode(Json.write(user), "UTF-8"))(CookieOptions(secure = isHttps, path = "/", maxAge = application.sessionTimeout.seconds, httpOnly = true)))
  }
  def getUserCookie: Option[AuthenticationUser] = {
    Option(request.getCookies).toList.flatten.find(_.getName == "koskiUser").map(_.getValue).map(c => URLDecoder.decode(c, "UTF-8")).map(Json.read[AuthenticationUser])
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
    if (isCasSsoUsed) {
      redirect(application.config.getString("opintopolku.virkailija.url") + "/cas/login?service=" + casServiceUrl)
    } else {
      redirect("/")
    }
  }

  def redirectToLogout = {
    if (isCasSsoUsed) {
      redirect(application.config.getString("opintopolku.virkailija.url") + "/cas/logout?service=" + koskiRoot)
    } else {
      redirect("/")
    }
  }

  def isCasSsoUsed = application.config.hasPath("opintopolku.virkailija.url")
}
