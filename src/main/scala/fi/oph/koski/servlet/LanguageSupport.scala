package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.UserLanguage

trait LanguageSupport extends KoskiSpecificBaseServlet {
  def application: KoskiApplication

  /**
   * Sivunlatauksen kieli haetaan ohi käyttäjävälimuistin, jotta asiointikielen vaihto näkyy heti.
   * Tulos muistetaan pyynnön ajaksi, koska sitä kysytään renderöinnin aikana monta kertaa.
   * Ilman sessiota (lander, suoritusjako) käytetään kansalaisen omaa valintaa tai domainia.
   */
  def lang: String =
    Option(request.getAttribute(UserLanguage.LangAttribute)).map(_.toString).getOrElse {
      val resolved = koskiSessionOption
        .map(session => UserLanguage.resolveLanguageFresh(
          session.user, application.directoryClient, request, application.config))
        .getOrElse(UserLanguage.languageFromCookieOrDomain(request, application.config))
      request.setAttribute(UserLanguage.LangAttribute, resolved)
      resolved
    }

  def t(key: String): String = application.koskiLocalizationRepository.get(key).get(lang)

  def langFromDomain: String = UserLanguage.languageFromDomain(request, application.config)

  // Kansalaisen kielivalinta säilyy evästeessä; oletus asetetaan vain jos valintaa ei vielä ole.
  def setLangCookieFromDomainIfNecessary: Unit =
    if (UserLanguage.languageFromCookie(request).isEmpty) {
      UserLanguage.setLanguageCookie(langFromDomain, response)
    }
}
