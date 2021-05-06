package fi.oph.koski

import fi.oph.koski.jettylauncher.JettyLauncher
import fi.oph.koski.log.Logging
import fi.oph.koski.util.PortChecker

object SharedJetty extends JettyLauncher(PortChecker.findFreeLocalPort, KoskiApplicationForTests) with Logging {
  logger.info("Start shared jetty for tests")
}
