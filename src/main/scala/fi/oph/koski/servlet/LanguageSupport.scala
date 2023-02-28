package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.UserLanguage.{getLanguageFromCookie, sanitizeLanguage, setLanguageCookie}

trait LanguageSupport extends KoskiSpecificBaseServlet {
  def application: KoskiApplication

  def lang: String = getLanguageFromCookie(request)
  def t(key: String): String = application.koskiLocalizationRepository.get(key).get(lang)

  def langFromDomain: String = if (request.getServerName == swedishDomain) {
    "sv"
  } else if(request.getServerName == englishDomain) {
    "en"
  } else {
    "fi"
  }

  def langFromCookie: Option[String] = sanitizeLanguage(request.cookies.get("lang"))

  def setLangCookieFromDomainIfNecessary: Unit = if (langFromCookie.isEmpty) {
    setLanguageCookie(langFromDomain, response)
  }

  private def swedishDomain = application.config.getString("koski.oppija.domain.sv")
  private def englishDomain = application.config.getString("koski.oppija.domain.en")
}
