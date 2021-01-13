package fi.oph.koski.servlet

import fi.oph.koski.config.Environment
import fi.oph.koski.html.{EiRaameja, Oppija, Raamit}
import fi.oph.common.koskiuser.AuthenticationSupport
import org.scalatra.servlet.RichRequest

trait OmaOpintopolkuSupport extends AuthenticationSupport with LanguageSupport {
  def oppijaRaamit: Raamit = if (oppijaRaamitSet || useOppijaRaamitProxy) Oppija(koskiSessionOption, request, shibbolethUrl) else EiRaameja
  def shibbolethCookieFound: Boolean = OmaOpintopolkuSupport.shibbolethCookieFound(request)
  def shibbolethUrl: String = application.config.getString("identification.url." + langFromCookie.getOrElse(langFromDomain))
  def oppijaRaamitSet: Boolean = isCloudEnvironment
  private val useOppijaRaamitProxy = application.config.hasPath("oppijaRaamitProxy")
  private lazy val isCloudEnvironment = !Environment.isLocalDevelopmentEnvironment
}

object OmaOpintopolkuSupport {
  def shibbolethCookieFound(request: RichRequest): Boolean = request.cookies.keys.exists(_.startsWith("_shibsession_"))
}
