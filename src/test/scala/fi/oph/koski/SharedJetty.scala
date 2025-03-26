package fi.oph.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.jettylauncher.JettyLauncher
import fi.oph.koski.log.Logging

class SharedJetty(koskiApplication: KoskiApplication)
  extends JettyLauncher(7031, koskiApplication) with Logging {
  logger.info("Start shared jetty for tests")
}
