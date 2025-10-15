package fi.oph.koski.servlet

import fi.oph.koski.config.Environment
import fi.oph.koski.html.{EiRaameja, Oppija, Raamit}
import fi.oph.koski.koskiuser.KoskiCookieAndBasicAuthenticationSupport

trait OmaOpintopolkuSupport extends KoskiCookieAndBasicAuthenticationSupport with LanguageSupport {
  private val oppijaRaamitSet: Boolean = Environment.isServerEnvironment(application.config)
  private def loginUrl: String = application.config.getString("identification.url." + langFromCookie.getOrElse(langFromDomain))
  private val useOppijaRaamitProxy = application.config.hasPath("oppijaRaamitProxy")

  protected def oppijaRaamit: Raamit =
    if (oppijaRaamitSet || useOppijaRaamitProxy) {
      Oppija(koskiSessionOption, request, loginUrl)
    } else {
      EiRaameja
    }
}
