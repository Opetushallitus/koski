package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiUserLanguage.{getLanguageFromCookie, sanitizeLanguage}

trait LanguageSupport extends KoskiBaseServlet {
  def application: KoskiApplication

  def lang: String = getLanguageFromCookie(request)

  def langFromDomain: String = if (request.getServerName == swedishDomain) {
    "sv"
  } else {
    "fi"
  }

  def langFromCookie: Option[String] = sanitizeLanguage(request.cookies.get("lang"))

  private def swedishDomain = application.config.getString("koski.oppija.domain.sv")
}
