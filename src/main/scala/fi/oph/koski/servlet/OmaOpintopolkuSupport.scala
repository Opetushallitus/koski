package fi.oph.koski.servlet

import fi.oph.koski.html.{EiRaameja, Oppija, Raamit}
import fi.oph.koski.koskiuser.KoskiUserLanguage.getLanguageFromCookie

trait OmaOpintopolkuSupport extends HtmlServlet {
  def oppijaRaamit: Raamit = if (raamitHeaderSet) Oppija(koskiSessionOption, shibbolethUrl) else EiRaameja
  def shibbolethUrl: String = application.config.getString("shibboleth.url." + lang)

  override def lang: String = {
    val langFromDomain = if (request.getServerName == swedishDomain) Some("sv") else None
    val language = getLanguageFromCookie(request, langFromDomain)
    logger.debug(s"Domain: ${request.getServerName}, swedish domain: $swedishDomain, lang: $language")
    language
  }

  private def swedishDomain = application.config.getString("koski.oppija.domain.sv")
}
