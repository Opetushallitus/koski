package fi.oph.koski.servlet

import java.net.URI

import fi.oph.koski.koskiuser.UserAuthenticationContext
import org.scalatra.{Cookie, CookieOptions, ScalatraBase}

trait CasSingleSignOnSupport extends ScalatraBase {
  def application: UserAuthenticationContext

  private def currentUrl = request.getRequestURL.toString

  def isHttps = !currentUrl.startsWith("http://localhost") // <- we don't get the https protocol correctly through the proxy, so we assume https

  def setServiceTicketCookie(ticket: String) = response.addCookie(Cookie("koskiServiceTicket", ticket)(CookieOptions(secure = isHttps, path = "/", maxAge = 3600)))

  def getServiceTicketCookie: Option[String] = Option(request.getCookies).toList.flatten.find(_.getName == "koskiServiceTicket").map(_.getValue)

  def removeServiceTicketCookie = response.addCookie(Cookie("koskiServiceTicket", "")(CookieOptions(secure = isHttps, path = "/", maxAge = 0)))

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


  def redirectToLogin = {
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
