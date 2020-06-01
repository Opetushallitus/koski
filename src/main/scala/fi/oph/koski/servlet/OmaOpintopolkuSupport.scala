package fi.oph.koski.servlet

import fi.oph.koski.html.{EiRaameja, Oppija, Raamit}
import fi.oph.koski.koskiuser.AuthenticationSupport
import org.scalatra.servlet.RichRequest

trait OmaOpintopolkuSupport extends AuthenticationSupport with LanguageSupport {
  def oppijaRaamit: Raamit = if (raamitHeaderSet || useOppijaRaamitProxy) Oppija(koskiSessionOption, request, shibbolethUrl) else EiRaameja
  def shibbolethCookieFound: Boolean = OmaOpintopolkuSupport.shibbolethCookieFound(request)
  def shibbolethUrl: String = application.config.getString("identification.url." + langFromCookie.getOrElse(langFromDomain))
  def raamitHeaderSet: Boolean
  private val useOppijaRaamitProxy = application.config.hasPath("oppijaRaamitProxy")
}

object OmaOpintopolkuSupport {
  def shibbolethCookieFound(request: RichRequest): Boolean = request.cookies.toMap.keys.exists(_.startsWith("_shibsession_"))
}
