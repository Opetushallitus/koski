package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{AuthenticationUser, UserLanguage}
import fi.oph.koski.koskiuser.UserLanguage.{sanitizeLanguage, setLanguageCookie}

trait LanguageSupport extends KoskiSpecificBaseServlet {
  def application: KoskiApplication

  def lang: String = langFromRequestAttribute.orElse(langFromCookie).getOrElse("fi")
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

  // Virkailijalla ei ole kielivalitsinta eikä domainpäättelyä: kieli tulee asiointikielestä. Ks. UserLanguage.
  def setLangCookieFromUserIfNecessary(user: AuthenticationUser): Unit =
    UserLanguage.setLanguageCookieFromUserIfNecessary(user, application.directoryClient, request, response)
      .foreach(request.setAttribute(UserLanguage.LangAttribute, _))

  // Tälle pyynnölle juuri ratkaistu kieli voittaa evästeen, koska vastaukseen asetettu eväste ei näy vielä
  // saman pyynnön request.cookiesissa.
  private def langFromRequestAttribute: Option[String] =
    Option(request.getAttribute(UserLanguage.LangAttribute)).map(_.toString)

  private def swedishDomain = application.config.getString("koski.oppija.domain.sv")
  private def englishDomain = application.config.getString("koski.oppija.domain.en")
}
