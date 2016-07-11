package fi.oph.koski.servlet

import java.net.URI

import fi.oph.koski.koskiuser.UserAuthenticationContext
import org.scalatra.ScalatraBase

trait CasSingleSignOnSupport extends ScalatraBase {
  def application: UserAuthenticationContext

  def currentUrl = request.getRequestURL.toString

  def casServiceUrl = fixProtocol {
    val subpath = request.getServletPath + request.getPathInfo
    (if (currentUrl.endsWith(subpath)) {
      currentUrl.substring(0, currentUrl.length - subpath.length)
    } else {
      currentUrl
    }) + "/user/cas"
  }

  private def fixProtocol(url: String) = if (url startsWith ("http://localhost")) {
    url
  } else {
    currentUrl.replace("http://", "https://") // <- we don't get the https protocol correctly, so we replace it manually
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
