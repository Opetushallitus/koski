package fi.oph.koski.servlet

import fi.oph.koski.html.{EiRaameja, Oppija, Raamit}

trait OppijaRaamitSupport extends HtmlServlet {
  def oppijaRaamit: Raamit = if (raamitHeaderSet) Oppija(koskiSessionOption, shibbolethUrl) else EiRaameja
  def shibbolethUrl: String = application.config.getString("shibboleth.url." + lang)
}
