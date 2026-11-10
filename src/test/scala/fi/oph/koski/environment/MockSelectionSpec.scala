package fi.oph.koski.environment

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.cache.{CacheManager, GlobalCacheManager}
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.henkilo.{Hetu, OpintopolkuHenkilöFacade, OppijanumeroRekisteriClient, RemoteOpintopolkuHenkilöFacade}
import fi.oph.koski.huoltaja.HuollettavatRepository
import fi.oph.koski.koodisto.{KoodistoPalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.omadataoauth2.OmaDataOAuth2DiscoveryServlet
import fi.oph.koski.luovutuspalvelu.v2clientlist.{LuovutuspalveluV2ClientListService, LuovutuspalveluV2XRoadConfigService}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.ovara.OvaraClient
import fi.oph.koski.pulssi.PrometheusRepository
import fi.oph.koski.sso.{CasService, SSOConfig}
import fi.oph.koski.todistus.swisscomclient.{SwisscomConfig, SwisscomConfigSecretsSource}
import fi.oph.koski.todistus.tiedote.{KituClient, TiedotuspalveluClient}
import fi.oph.koski.userdirectory.DirectoryClient
import fi.oph.koski.valpas.hakukooste.ValpasHakukoosteService
import fi.oph.koski.valpas.localization.ValpasLocalizationConfig
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.koski.virta.VirtaClient
import fi.oph.koski.ytr.{KoesuoritusService, YoTodistusService, YtrClient, YtrS3Config}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MockSelectionSpec extends AnyFreeSpec with Matchers {
  private implicit val cacheManager: CacheManager = GlobalCacheManager

  private def configFor(env: String): Config = KoskiApplication.defaultConfig
    .withValue("env", fromAnyRef(env))
    .withValue("opintopolku.virkailija.username", fromAnyRef("käyttäjä"))
    .withValue("opintopolku.virkailija.password", fromAnyRef("salasana"))

  private val mockS3Config = YtrS3Config("", "", "", "", "mock")

  // Reference.confin mock-arvoilla jokainen näistä päätyy valitsemaan mock-toteutuksen.
  private val selections: Seq[(String, Config => Any)] = Seq(
    "MockDirectoryClient" -> (c => DirectoryClient(c, new CasService(c))),
    "MockEPerusteetRepository" -> (c => EPerusteetRepository(c)),
    "MockKoodistoPalvelu" -> (c => KoodistoPalvelu.withoutCache(c)),
    "MockOrganisaatioRepository" -> (c => OrganisaatioRepository(c, MockKoodistoViitePalvelu)),
    "MockVirtaClient" -> (c => VirtaClient(new Hetu(false), c)),
    "MockYtrClient" -> (c => YtrClient(c)),
    "MockHuollettavatRepository" -> (c => HuollettavatRepository(c)),
    "MockLocalizationRepository" -> (c => LocalizationRepository(c, new ValpasLocalizationConfig)),
    "MockPrometheusRepository" -> (c => PrometheusRepository(c)),
    "MockOvaraClient" -> (c => OvaraClient(c)),
    // Lähde annetaan suoraan, koska SWISSCOM_SECRET_ID (documentation/todistus.md) vaihtaisi sen.
    "MockSwisscomClient" -> (c => SwisscomConfig(c, SwisscomConfigSecretsSource.MOCK_FROM_CONFIG)),
    "MockKoesuoritusService" -> (c => KoesuoritusService(c, mockS3Config)),
    "valpas.rajapäivät.useMock" -> (c => ValpasRajapäivätService(c)),
    "otuvaTokenEndpoint" -> (c => OppijanumeroRekisteriClient(c)),
    "login.security" -> (c => SSOConfig.requireMockLoginAllowed(c)),
    "MockLuovutuspalveluV2ClientListService" -> (c => LuovutuspalveluV2ClientListService(c)),
    "MockLuovutuspalveluV2XRoadConfigService" -> (c => LuovutuspalveluV2XRoadConfigService(c)),
  )

  private val localOnlySelections: Seq[(String, Config => Any)] = Seq(
    "MockKituClient" -> (c => KituClient(c)),
    "MockTiedotuspalveluClient" -> (c => TiedotuspalveluClient(c)),
  )

  // Näiden luominen muissa ympäristöissä vaatisi tietokantayhteyden, joten testataan vain tuotannossa.
  private val applicationSelections: Seq[(String, KoskiApplication => Any)] = Seq(
    "MockOpintopolkuHenkilöFacade" -> (a => OpintopolkuHenkilöFacade(a.config, a.masterDatabase.db, a.hetu, a.fixtureCreator, a.perustiedotRepository, a.perustiedotIndexer)),
    "MockHakukoosteService" -> (a => ValpasHakukoosteService(a)),
    "MockYoTodistusService" -> (a => YoTodistusService(a)),
    "opintopolku.oppija.url" -> (a => new OmaDataOAuth2DiscoveryServlet()(a)),
  )

  "Tuotannossa mock-toteutuksen valinta estetään" - {
    selections.foreach { case (name, select) =>
      name in {
        val thrown = intercept[IllegalStateException](select(configFor(Environment.Prod)))
        thrown.getMessage should include(name)
      }
    }

    applicationSelections.foreach { case (name, select) =>
      name in {
        val thrown = intercept[IllegalStateException](select(new KoskiApplication(configFor(Environment.Prod))))
        thrown.getMessage should include(name)
      }
    }

    "authentication-service.mockOid" in {
      val config = configFor(Environment.Prod).withValue("authentication-service.mockOid", fromAnyRef(true))
      val app = new KoskiApplication(config)
      val thrown = intercept[IllegalStateException] {
        RemoteOpintopolkuHenkilöFacade(config, app.perustiedotRepository, app.perustiedotIndexer)
      }
      thrown.getMessage should include("authentication-service.mockOid")
    }

    "omadataoauth2.luovutuspalveluBaseUrl" in {
      val config = configFor(Environment.Prod).withValue("opintopolku.oppija.url", fromAnyRef("https://opintopolku.fi"))
      val thrown = intercept[IllegalStateException](new OmaDataOAuth2DiscoveryServlet()(new KoskiApplication(config)))
      thrown.getMessage should include("omadataoauth2.luovutuspalveluBaseUrl")
    }
  }

  "Tuntemattomassa ympäristössä mock-toteutuksen valinta estetään" in {
    val thrown = intercept[IllegalStateException](YtrClient(configFor("OPHKoskiUusi")))
    thrown.getMessage should include("MockYtrClient")
  }

  "Muissa ympäristöissä mock-toteutus on sallittu" - {
    Seq(Environment.Dev, Environment.QA, Environment.Local, Environment.UnitTest).foreach { env =>
      env - {
        selections.foreach { case (name, select) =>
          name in {
            noException should be thrownBy select(configFor(env))
          }
        }
      }
    }
  }

  "Kielitutkintotodistusten tiedotteiden mock-toteutukset" - {
    Seq(Environment.Prod, Environment.Dev, Environment.QA, "OPHKoskiUusi").foreach { env =>
      s"estetään ympäristössä $env" - {
        localOnlySelections.foreach { case (name, select) =>
          name in {
            val thrown = intercept[IllegalStateException](select(configFor(env)))
            thrown.getMessage should include(name)
          }
        }
      }
    }

    Seq(Environment.Local, Environment.UnitTest).foreach { env =>
      s"sallitaan ympäristössä $env" - {
        localOnlySelections.foreach { case (name, select) =>
          name in {
            noException should be thrownBy select(configFor(env))
          }
        }
      }
    }
  }
}
