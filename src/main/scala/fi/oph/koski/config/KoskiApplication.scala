package fi.oph.koski.config

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.arvosana.ArviointiasteikkoRepository
import fi.oph.koski.cache.CachingProxy
import fi.oph.koski.cache.CachingStrategy.cacheAllRefresh
import fi.oph.koski.db._
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.fixture.Fixtures
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.koodisto.{KoodistoPalvelu, KoodistoViitePalvelu}
import fi.oph.koski.log.{Logging, TimedProxy}
import fi.oph.koski.opiskeluoikeus.{CompositeOpiskeluOikeusRepository, OpiskeluOikeusRepository, PostgresOpiskeluOikeusRepository}
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.koski.KoskiValidator
import fi.oph.koski.koskiuser.{DirectoryClientFactory, UserOrganisationsRepository}
import fi.oph.koski.tutkinto.TutkintoRepository
import fi.oph.koski.virta.{VirtaClient, VirtaOpiskeluoikeusRepository}
import fi.oph.koski.ytr.{YlioppilasTutkintoRekisteri, YtrMock, YtrOpiskeluoikeusRepository}

object KoskiApplication {
  val defaultConfig = ConfigFactory.load

  def apply: KoskiApplication = apply(Map.empty)

  def apply(overrides: Map[String, String] = Map.empty): KoskiApplication = {
    new KoskiApplication(config(overrides))
  }

  def config(overrides: Map[String, String] = Map.empty) = overrides.toList.foldLeft(defaultConfig)({ case (config, (key, value)) => config.withValue(key, fromAnyRef(value)) })
}

class KoskiApplication(val config: Config) extends Logging {
  lazy val organisaatioRepository = OrganisaatioRepository(config, koodistoViitePalvelu)
  lazy val directoryClient = DirectoryClientFactory.directoryClient(config)
  lazy val tutkintoRepository = CachingProxy(cacheAllRefresh(3600, 100), TutkintoRepository(EPerusteetRepository.apply(config), arviointiAsteikot, koodistoViitePalvelu))
  lazy val oppilaitosRepository = new OppilaitosRepository(organisaatioRepository)
  lazy val koodistoPalvelu = KoodistoPalvelu.apply(config)
  lazy val koodistoViitePalvelu = new KoodistoViitePalvelu(koodistoPalvelu)
  lazy val arviointiAsteikot = ArviointiasteikkoRepository(koodistoViitePalvelu)
  lazy val userRepository = UserOrganisationsRepository(config, organisaatioRepository)
  lazy val database = new KoskiDatabase(config)
  lazy val virtaClient = VirtaClient(config)
  lazy val ytrClient = YlioppilasTutkintoRekisteri(config)
  lazy val oppijaRepository = OppijaRepository(config, database, koodistoViitePalvelu, virtaClient, ytrClient)
  lazy val historyRepository = OpiskeluoikeusHistoryRepository(database.db)
  lazy val virta = TimedProxy[OpiskeluOikeusRepository](VirtaOpiskeluoikeusRepository(virtaClient, oppijaRepository, oppilaitosRepository, koodistoViitePalvelu, Some(validator)))
  lazy val possu = TimedProxy[OpiskeluOikeusRepository](new PostgresOpiskeluOikeusRepository(database.db, historyRepository))
  lazy val ytr = TimedProxy[OpiskeluOikeusRepository](YtrOpiskeluoikeusRepository(ytrClient, oppijaRepository, organisaatioRepository, oppilaitosRepository, koodistoViitePalvelu, Some(validator)))
  lazy val opiskeluOikeusRepository = new CompositeOpiskeluOikeusRepository(List(possu, virta, ytr))
  lazy val validator: KoskiValidator = new KoskiValidator(tutkintoRepository, koodistoViitePalvelu, organisaatioRepository)

  def resetFixtures = Fixtures.resetFixtures(config, database, opiskeluOikeusRepository, oppijaRepository, validator)

  def invalidateCaches = List(organisaatioRepository, directoryClient, tutkintoRepository, koodistoPalvelu, userRepository, oppijaRepository).foreach(_.invalidateCache)
}