package fi.oph.tor.config

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.cache.CachingProxy
import fi.oph.tor.cache.CachingStrategy.cacheAllRefresh
import fi.oph.tor.db._
import fi.oph.tor.eperusteet.EPerusteetRepository
import fi.oph.tor.fixture.Fixtures
import fi.oph.tor.history.OpiskeluoikeusHistoryRepository
import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViitePalvelu}
import fi.oph.tor.log.{Logging, TimedProxy}
import fi.oph.tor.opiskeluoikeus.{OpiskeluOikeusRepository, OpiskeluOikeusTestData, PostgresOpiskeluOikeusRepository, TorDatabaseFixtureCreator}
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.organisaatio.OrganisaatioRepository
import fi.oph.tor.tor.{TorValidator, ValidationAndResolvingContext}
import fi.oph.tor.toruser.{DirectoryClientFactory, UserOrganisationsRepository}
import fi.oph.tor.tutkinto.TutkintoRepository
import fi.vm.sade.security.ldap.DirectoryClient

object TorApplication {
  def apply: TorApplication = apply(Map.empty)

  def apply(overrides: Map[String, String] = Map.empty): TorApplication = {
    new TorApplication(config(overrides))
  }

  def config(overrides: Map[String, String] = Map.empty) = overrides.toList.foldLeft(ConfigFactory.load)({ case (config, (key, value)) => config.withValue(key, fromAnyRef(value)) })
}

class TorApplication(val config: Config) extends Logging {
  lazy val organisaatioRepository: OrganisaatioRepository = OrganisaatioRepository(config, koodistoViitePalvelu)
  lazy val directoryClient: DirectoryClient = DirectoryClientFactory.directoryClient(config)
  lazy val tutkintoRepository = CachingProxy(cacheAllRefresh(3600, 100), TutkintoRepository(EPerusteetRepository.apply(config), arviointiAsteikot, koodistoViitePalvelu))
  lazy val oppilaitosRepository = new OppilaitosRepository(organisaatioRepository)
  lazy val koodistoPalvelu = KoodistoPalvelu.apply(config)
  lazy val koodistoViitePalvelu = new KoodistoViitePalvelu(koodistoPalvelu)
  lazy val arviointiAsteikot = ArviointiasteikkoRepository(koodistoViitePalvelu)
  lazy val userRepository = UserOrganisationsRepository(config, organisaatioRepository)
  lazy val database = new TorDatabase(config)
  lazy val oppijaRepository = OppijaRepository(config, database, koodistoViitePalvelu)
  lazy val historyRepository = OpiskeluoikeusHistoryRepository(database.db)
  lazy val opiskeluOikeusRepository = TimedProxy[OpiskeluOikeusRepository](new PostgresOpiskeluOikeusRepository(database.db, historyRepository))
  lazy val validator: TorValidator = new TorValidator(tutkintoRepository, koodistoViitePalvelu, organisaatioRepository)

  def resetFixtures = if(Fixtures.shouldUseFixtures(config)) {
    new TorDatabaseFixtureCreator(database, opiskeluOikeusRepository, oppijaRepository, validator).resetFixtures
    oppijaRepository.resetFixtures
    logger.info("Reset application fixtures")
  }
}