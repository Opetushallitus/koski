package fi.oph.koski.servlet

import fi.oph.koski.config.Environment
import fi.oph.koski.html.{EiRaameja, Raamit, Virkailija}

trait VirkailijaHtmlServlet extends KoskiHtmlServlet {
  private val useVirkailijaRaamitProxy = application.config.hasPath("virkailijaRaamitProxy")

  protected val virkailijaRaamitSet: Boolean = Environment.isServerEnvironment(application.config)

  protected def virkailijaRaamit: Raamit = if (virkailijaRaamitSet || useVirkailijaRaamitProxy) Virkailija else EiRaameja
}
