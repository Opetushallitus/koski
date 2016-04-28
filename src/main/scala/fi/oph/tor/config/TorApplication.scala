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
import fi.oph.tor.opiskeluoikeus.{CompositeOpiskeluOikeusRepository, OpiskeluOikeusRepository, PostgresOpiskeluOikeusRepository}
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.organisaatio.OrganisaatioRepository
import fi.oph.tor.tor.TorValidator
import fi.oph.tor.toruser.{DirectoryClientFactory, UserOrganisationsRepository}
import fi.oph.tor.tutkinto.TutkintoRepository
import fi.oph.tor.virta.{VirtaClient, VirtaOpiskeluoikeusRepository}

object TorApplication {
  val defaultConfig = ConfigFactory.load

  def apply: TorApplication = apply(Map.empty)

  def apply(overrides: Map[String, String] = Map.empty): TorApplication = {
    new TorApplication(config(overrides))
  }

  def config(overrides: Map[String, String] = Map.empty) = overrides.toList.foldLeft(defaultConfig)({ case (config, (key, value)) => config.withValue(key, fromAnyRef(value)) })
}

class TorApplication(val config: Config) extends Logging {
  lazy val organisaatioRepository = OrganisaatioRepository(config, koodistoViitePalvelu)
  lazy val directoryClient = DirectoryClientFactory.directoryClient(config)
  lazy val tutkintoRepository = CachingProxy(cacheAllRefresh(3600, 100), TutkintoRepository(EPerusteetRepository.apply(config), arviointiAsteikot, koodistoViitePalvelu))
  lazy val oppilaitosRepository = new OppilaitosRepository(organisaatioRepository)
  lazy val koodistoPalvelu = KoodistoPalvelu.apply(config)
  lazy val koodistoViitePalvelu = new KoodistoViitePalvelu(koodistoPalvelu)
  lazy val arviointiAsteikot = ArviointiasteikkoRepository(koodistoViitePalvelu)
  lazy val userRepository = UserOrganisationsRepository(config, organisaatioRepository)
  lazy val database = new TorDatabase(config)
  lazy val oppijaRepository = OppijaRepository(config, database, koodistoViitePalvelu)
  lazy val historyRepository = OpiskeluoikeusHistoryRepository(database.db)
  lazy val virta = VirtaOpiskeluoikeusRepository(VirtaClient(config), oppijaRepository, oppilaitosRepository, koodistoViitePalvelu)
  lazy val possu = TimedProxy[OpiskeluOikeusRepository](new PostgresOpiskeluOikeusRepository(database.db, historyRepository))
  lazy val opiskeluOikeusRepository = new CompositeOpiskeluOikeusRepository(List(possu, virta))
  lazy val validator: TorValidator = new TorValidator(tutkintoRepository, koodistoViitePalvelu, organisaatioRepository)

  def resetFixtures = Fixtures.resetFixtures(config, database, opiskeluOikeusRepository, oppijaRepository, validator)

  def invalidateCaches = List(organisaatioRepository, directoryClient, tutkintoRepository, koodistoPalvelu, userRepository, oppijaRepository).foreach(_.invalidateCache)
}