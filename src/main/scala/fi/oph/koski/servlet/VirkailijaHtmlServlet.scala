package fi.oph.koski.servlet

import fi.oph.koski.config.Environment
import fi.oph.koski.html.{EiRaameja, Raamit, Virkailija}

trait VirkailijaHtmlServlet extends KoskiHtmlServlet {
  def virkailijaRaamitSet: Boolean = isCloudEnvironment
  def virkailijaRaamit: Raamit = if (virkailijaRaamitSet || useVirkailijaRaamitProxy) Virkailija else EiRaameja

  private val useVirkailijaRaamitProxy = application.config.hasPath("virkailijaRaamitProxy")

  private lazy val isCloudEnvironment = !Environment.isLocalDevelopmentEnvironment
}
