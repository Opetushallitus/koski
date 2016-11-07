package fi.oph.koski.integrationtest

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.jettylauncher.JettyLauncher
import fi.oph.koski.log.LogTester
import fi.oph.koski.util.PortChecker
import org.apache.log4j.Level.{ERROR, FATAL}
import org.eclipse.jetty.server.Server
import org.scalatest.{FreeSpec, Matchers}

// This test should be run against the KoskiDev test environment, you need to provide the corresponding configuration with -Dconfig.resource or
// using System.properties
class ServerStartupIntegrationTest extends FreeSpec with Matchers with LogTester {
  "Server starts without errors" taggedAs(KoskiDevEnvironment) in {
    if (!KoskiApplication.defaultConfig.hasPath("opintopolku.virkailija.url")) {
      System.err.println("ServerStartupIntegrationTest run without opintopolku.virkailija.url => tests mocks only")
    }
    setup
    new JettyLauncher(PortChecker.findFreeLocalPort) {
      override def configureLogging = {}
    }.start
    getLogMessages.find(m => m.getLevel == ERROR || m.getLevel == FATAL) should be(empty)
    getLogMessages.last.getMessage.toString should startWith("Started")
  }
}