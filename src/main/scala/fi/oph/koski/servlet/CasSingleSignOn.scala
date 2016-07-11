package fi.oph.koski.servlet

import java.net.URI

import fi.oph.koski.koskiuser.UserAuthenticationContext
import org.apache.commons.lang3.StringUtils
import org.scalatra.ScalatraBase

trait CasSingleSignOnSupport extends ScalatraBase {
  def application: UserAuthenticationContext

  def currentUrl = request.getRequestURL.toString

  def redirectToLogin = {
    if (isCasSsoUsed) {
      redirect(application.config.getString("opintopolku.virkailija.url") + "/cas/login?service=" + currentUrl)
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
