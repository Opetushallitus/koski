package fi.oph.koski.config

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.arvosana.ArviointiasteikkoRepository
import fi.oph.koski.cache.CacheManager
import fi.oph.koski.db._
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.healthcheck.HealthCheck
import fi.oph.koski.henkilo.{AuthenticationServiceClient, HenkilöRepository, KoskiHenkilöCacheUpdater}
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.koodisto.{KoodistoPalvelu, KoodistoViitePalvelu}
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.{Logging, TimedProxy}
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedotRepository, PerustiedotSearchIndex, OpiskeluoikeudenPerustiedotIndexer, PerustiedotIndexUpdater}
import fi.oph.koski.pulssi.{KoskiPulssi, PrometheusRepository}
import fi.oph.koski.schedule.KoskiScheduledTasks
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.sso.KoskiSessionRepository
import fi.oph.koski.tiedonsiirto.{IPService, TiedonsiirtoFailureMailer, TiedonsiirtoService}
import fi.oph.koski.tutkinto.TutkintoRepository
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.virta.{VirtaAccessChecker, VirtaClient, VirtaOpiskeluoikeusRepository}
import fi.oph.koski.ytr.{YtrAccessChecker, YtrClient, YtrOpiskeluoikeusRepository}

object KoskiApplication {
  lazy val defaultConfig = ConfigFactory.load

  def apply: KoskiApplication = apply(defaultConfig)

  def apply(config: Config): KoskiApplication = new KoskiApplication(config)
}

class KoskiApplication(val config: Config, implicit val cacheManager: CacheManager = new CacheManager) extends Logging with UserAuthenticationContext {
  lazy val organisaatioRepository = OrganisaatioRepository(config, koodistoViitePalvelu)
  lazy val directoryClient = DirectoryClientFactory.directoryClient(config)
  lazy val tutkintoRepository = TutkintoRepository(EPerusteetRepository.apply(config), arviointiAsteikot, koodistoViitePalvelu)
  lazy val oppilaitosRepository = new OppilaitosRepository(organisaatioRepository)
  lazy val koodistoPalvelu = KoodistoPalvelu.apply(config)
  lazy val koodistoViitePalvelu = KoodistoViitePalvelu(koodistoPalvelu)
  lazy val arviointiAsteikot = ArviointiasteikkoRepository(koodistoViitePalvelu)
  lazy val authenticationServiceClient = AuthenticationServiceClient(config, masterDatabase.db, perustiedotRepository, perustiedotIndexer)
  lazy val käyttöoikeusRepository = new KäyttöoikeusRepository(authenticationServiceClient, organisaatioRepository, directoryClient)
  lazy val userRepository = new KoskiUserRepository(authenticationServiceClient)
  lazy val masterDatabase = KoskiDatabase.master(config)
  lazy val replicaDatabase = KoskiDatabase.replica(config, masterDatabase)
  lazy val virtaClient = VirtaClient(config)
  lazy val ytrClient = YtrClient(config)
  lazy val virtaAccessChecker = new VirtaAccessChecker(käyttöoikeusRepository)
  lazy val ytrAccessChecker = new YtrAccessChecker(käyttöoikeusRepository)
  lazy val henkilöRepository = HenkilöRepository(this)
  lazy val historyRepository = OpiskeluoikeusHistoryRepository(masterDatabase.db)
  lazy val virta = TimedProxy[AuxiliaryOpiskeluoikeusRepository](VirtaOpiskeluoikeusRepository(virtaClient, henkilöRepository, oppilaitosRepository, koodistoViitePalvelu, virtaAccessChecker, Some(validator)))
  lazy val henkilöCacheUpdater = new KoskiHenkilöCacheUpdater(masterDatabase.db, henkilöRepository)
  lazy val possu = TimedProxy[OpiskeluoikeusRepository](new PostgresOpiskeluoikeusRepository(masterDatabase.db, historyRepository, henkilöCacheUpdater))
  lazy val ytr = TimedProxy[AuxiliaryOpiskeluoikeusRepository](YtrOpiskeluoikeusRepository(ytrClient, henkilöRepository, organisaatioRepository, oppilaitosRepository, koodistoViitePalvelu, ytrAccessChecker, Some(validator)))
  lazy val opiskeluoikeusRepository = new CompositeOpiskeluoikeusRepository(possu, List(virta, ytr))
  lazy val opiskeluoikeusQueryRepository = new OpiskeluoikeusQueryService(masterDatabase.db)
  lazy val validator: KoskiValidator = new KoskiValidator(tutkintoRepository, koodistoViitePalvelu, organisaatioRepository)
  lazy val perustiedotIndex = new PerustiedotSearchIndex(config)
  lazy val perustiedotRepository = new OpiskeluoikeudenPerustiedotRepository(perustiedotIndex, opiskeluoikeusQueryRepository)
  lazy val perustiedotIndexer = new OpiskeluoikeudenPerustiedotIndexer(config, perustiedotIndex, opiskeluoikeusQueryRepository)
  lazy val oppijaFacade = new KoskiOppijaFacade(henkilöRepository, opiskeluoikeusRepository, historyRepository, perustiedotIndexer, config)
  lazy val sessionTimeout = SessionTimeout(config)
  lazy val koskiSessionRepository = new KoskiSessionRepository(masterDatabase.db, sessionTimeout)
  lazy val fixtureCreator = new FixtureCreator(this)
  lazy val tiedonsiirtoService = new TiedonsiirtoService(masterDatabase.db, new TiedonsiirtoFailureMailer(config, authenticationServiceClient), organisaatioRepository, henkilöRepository, koodistoViitePalvelu, userRepository)
  lazy val healthCheck = HealthCheck(this)
  lazy val scheduledTasks = new KoskiScheduledTasks(this)
  lazy val ipService = new IPService(masterDatabase.db)
  lazy val prometheusRepository = PrometheusRepository(config)
  lazy val koskiPulssi = KoskiPulssi(this)
  lazy val basicAuthSecurity = new BasicAuthSecurity(masterDatabase.db, config)
}