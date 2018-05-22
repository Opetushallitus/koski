package fi.oph.koski.servlet

import fi.oph.koski.html.{EiRaameja, Oppija, Raamit}
import fi.oph.koski.koskiuser.{AuthenticationSupport, KoskiUserLanguage}

trait OmaOpintopolkuSupport extends AuthenticationSupport with LanguageSupport {
  def oppijaRaamit: Raamit = if (raamitHeaderSet) Oppija(koskiSessionOption, shibbolethUrl) else EiRaameja
  def shibbolethCookieFound: Boolean = request.cookies.exists { case (key, _) => key.startsWith("_shibsession_") }
  def shibbolethUrl: String = application.config.getString("shibboleth.url." + langFromCookie.getOrElse(langFromDomain))
  def raamitHeaderSet: Boolean
}
