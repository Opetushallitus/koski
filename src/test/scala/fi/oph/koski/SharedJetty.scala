package fi.oph.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.jettylauncher.JettyLauncher
import fi.oph.koski.log.Logging

class SharedJetty(koskiApplication: KoskiApplication)
  // Portti 0 = käyttöjärjestelmä varaa vapaan portin bindissä. Kiinteä portti
  // esti kahden palvelimen käynnistämisen samassa JVM:ssä, mikä kaatoi
  // jälkimmäisen BindExceptioniin.
  extends JettyLauncher(0, koskiApplication) with Logging {
  logger.info("Start shared jetty for tests")
}
