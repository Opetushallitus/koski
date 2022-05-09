package fi.oph.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.jettylauncher.JettyLauncher
import fi.oph.koski.log.Logging
import fi.oph.koski.util.PortChecker

class SharedJetty(koskiApplication: KoskiApplication)
  extends JettyLauncher(PortChecker.findFreeLocalPort, koskiApplication) with Logging {
  logger.info("Start shared jetty for tests")
}
