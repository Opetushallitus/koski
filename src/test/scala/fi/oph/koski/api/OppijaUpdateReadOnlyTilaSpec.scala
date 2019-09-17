package fi.oph.koski.api

import java.time.LocalDate

import com.typesafe.config.ConfigFactory
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.config.KoskiApplication.defaultConfig
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.jettylauncher.{JettyLauncher, TestConfig}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, Oppija}
import fi.oph.koski.util.PortChecker
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

class OppijaUpdateReadOnlyTilaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with BeforeAndAfterAll {
  "Opiskeluoikeuden lisääminen readonly tilassa" - {
    "ei onnistu v1-apilla" in {
      putOppija(Oppija(MockOppijat.tyhjä, List(defaultOpiskeluoikeus))) {
        verifyResponseStatus(503, KoskiErrorCategory.unavailable.readOnly())
      }
    }

    "ei onnistu v2-apilla" in {
      putOppijaV2(Oppija(MockOppijat.tyhjä, List(defaultOpiskeluoikeus))) {
        verifyResponseStatus(503, KoskiErrorCategory.unavailable.readOnly())
      }
    }
  }

  "Opiskeluoikeuden muokkaaminen" - {
    "ei onnistu v1-apilla" in {
      lastOpiskeluoikeusByHetu(MockOppijat.eerola).asInstanceOf[AmmatillinenOpiskeluoikeus]
        .copy(arvioituPäättymispäivä = Some(LocalDate.of(2020, 1, 1)))

      putOppija(Oppija(MockOppijat.eerola, List(lastOpiskeluoikeusByHetu(MockOppijat.eerola).asInstanceOf[AmmatillinenOpiskeluoikeus]))) {
        verifyResponseStatus(503, KoskiErrorCategory.unavailable.readOnly())
      }
    }

    "ei onnistu v2-apilla" in {
      lastOpiskeluoikeusByHetu(MockOppijat.eerola).asInstanceOf[AmmatillinenOpiskeluoikeus]
        .copy(arvioituPäättymispäivä = Some(LocalDate.of(2020, 1, 1)))

      putOppijaV2(Oppija(MockOppijat.eerola, List(lastOpiskeluoikeusByHetu(MockOppijat.eerola).asInstanceOf[AmmatillinenOpiskeluoikeus]))) {
        verifyResponseStatus(503, KoskiErrorCategory.unavailable.readOnly())
      }
    }
  }

  private var running = false
  private lazy val jetty = new JettyLauncher(PortChecker.findFreeLocalPort, readOnlyApplication)
  override def baseUrl: String = synchronized {
    if (!running) {
      jetty.start
    }
    jetty.baseUrl
  }

  override protected def afterAll(): Unit = jetty.stop
  private def readOnlyApplication = KoskiApplication {
    val testConfig = TestConfig.overrides.withFallback(defaultConfig)
    ConfigFactory.parseString("koski.oppija.readOnly = true").withFallback(testConfig)
  }
}
