package fi.oph.koski.config

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.cache.CacheManager
import fi.oph.koski.db._
import fi.oph.koski.elasticsearch.ElasticSearch
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.healthcheck.HealthCheck
import fi.oph.koski.henkilo.{HenkilöRepository, KoskiHenkilöCache, OpintopolkuHenkilöFacade}
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.koodisto.{KoodistoCreator, KoodistoPalvelu, KoodistoViitePalvelu}
import fi.oph.koski.koskiuser._
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.{Logging, TimedProxy}
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.perustiedot.{KoskiElasticSearchIndex, OpiskeluoikeudenPerustiedotIndexer, OpiskeluoikeudenPerustiedotRepository, PerustiedotSyncRepository}
import fi.oph.koski.pulssi.{KoskiPulssi, PrometheusRepository}
import fi.oph.koski.schedule.{KoskiScheduledTasks, PerustiedotSyncScheduler}
import fi.oph.koski.sso.KoskiSessionRepository
import fi.oph.koski.tiedonsiirto.{IPService, TiedonsiirtoFailureMailer, TiedonsiirtoService}
import fi.oph.koski.tutkinto.TutkintoRepository
import fi.oph.koski.userdirectory.DirectoryClient
import fi.oph.koski.util.OidGenerator
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.virta.{VirtaAccessChecker, VirtaClient, VirtaOpiskeluoikeusRepository}
import fi.oph.koski.ytr.{YtrAccessChecker, YtrClient, YtrOpiskeluoikeusRepository}

import scala.collection.immutable
import scala.concurrent.Future

object KoskiApplication {
  lazy val defaultConfig = ConfigFactory.load

  def apply: KoskiApplication = apply(defaultConfig)

  def apply(config: Config): KoskiApplication = new KoskiApplication(config)
}

class KoskiApplication(val config: Config, implicit val cacheManager: CacheManager = new CacheManager) extends Logging with UserAuthenticationContext with GlobalExecutionContext {
  lazy val organisaatioRepository = OrganisaatioRepository(config, koodistoViitePalvelu)
  lazy val directoryClient = DirectoryClient(config)
  lazy val tutkintoRepository = TutkintoRepository(EPerusteetRepository.apply(config), koodistoViitePalvelu)
  lazy val oppilaitosRepository = new OppilaitosRepository(organisaatioRepository)
  lazy val koodistoPalvelu = KoodistoPalvelu.apply(config)
  lazy val koodistoViitePalvelu = KoodistoViitePalvelu(koodistoPalvelu)
  lazy val opintopolkuHenkilöFacade = OpintopolkuHenkilöFacade(config, masterDatabase.db, perustiedotRepository, elasticSearch)
  lazy val käyttöoikeusRepository = new KäyttöoikeusRepository(organisaatioRepository, directoryClient)
  lazy val userRepository = new KoskiUserRepository(opintopolkuHenkilöFacade)
  lazy val masterDatabase = KoskiDatabase.master(config)
  lazy val replicaDatabase = KoskiDatabase.replica(config, masterDatabase)
  lazy val virtaClient = VirtaClient(config)
  lazy val ytrClient = YtrClient(config)
  lazy val virtaAccessChecker = new VirtaAccessChecker(käyttöoikeusRepository)
  lazy val ytrAccessChecker = new YtrAccessChecker(käyttöoikeusRepository)
  lazy val henkilöRepository = HenkilöRepository(this)
  lazy val historyRepository = OpiskeluoikeusHistoryRepository(masterDatabase.db)
  lazy val virta = TimedProxy[AuxiliaryOpiskeluoikeusRepository](VirtaOpiskeluoikeusRepository(virtaClient, henkilöRepository, oppilaitosRepository, koodistoViitePalvelu, virtaAccessChecker, Some(validator)))
  lazy val henkilöCache = new KoskiHenkilöCache(masterDatabase.db, henkilöRepository)
  lazy val possu = TimedProxy[OpiskeluoikeusRepository](new PostgresOpiskeluoikeusRepository(masterDatabase.db, historyRepository, henkilöCache, oidGenerator, henkilöRepository.opintopolku, perustiedotSyncRepository))
  lazy val ytr = TimedProxy[AuxiliaryOpiskeluoikeusRepository](YtrOpiskeluoikeusRepository(ytrClient, henkilöRepository, organisaatioRepository, oppilaitosRepository, koodistoViitePalvelu, ytrAccessChecker, Some(validator)))
  lazy val opiskeluoikeusRepository = new CompositeOpiskeluoikeusRepository(possu, List(virta, ytr))
  lazy val opiskeluoikeusQueryRepository = new OpiskeluoikeusQueryService(replicaDatabase.db)
  lazy val validator: KoskiValidator = new KoskiValidator(tutkintoRepository, koodistoViitePalvelu, organisaatioRepository, possu, henkilöRepository.opintopolku)
  lazy val elasticSearch = ElasticSearch(config)
  lazy val koskiElasticSearchIndex = new KoskiElasticSearchIndex(elasticSearch)
  lazy val perustiedotRepository = new OpiskeluoikeudenPerustiedotRepository(koskiElasticSearchIndex, opiskeluoikeusQueryRepository)
  lazy val perustiedotSyncRepository = new PerustiedotSyncRepository(masterDatabase.db)
  lazy val perustiedotIndexer = new OpiskeluoikeudenPerustiedotIndexer(config, koskiElasticSearchIndex, opiskeluoikeusQueryRepository, perustiedotSyncRepository)
  lazy val perustiedotSyncScheduler = new PerustiedotSyncScheduler(this)
  lazy val oppijaFacade = new KoskiOppijaFacade(henkilöRepository, henkilöCache, opiskeluoikeusRepository, historyRepository, perustiedotIndexer, config)
  lazy val sessionTimeout = SessionTimeout(config)
  lazy val koskiSessionRepository = new KoskiSessionRepository(masterDatabase.db, sessionTimeout)
  lazy val fixtureCreator = new FixtureCreator(this)
  lazy val tiedonsiirtoService = new TiedonsiirtoService(koskiElasticSearchIndex, new TiedonsiirtoFailureMailer(config, opintopolkuHenkilöFacade), organisaatioRepository, henkilöRepository, koodistoViitePalvelu, userRepository)
  lazy val healthCheck = HealthCheck(this)
  lazy val scheduledTasks = new KoskiScheduledTasks(this)
  lazy val ipService = new IPService(masterDatabase.db)
  lazy val prometheusRepository = PrometheusRepository(config)
  lazy val koskiPulssi = KoskiPulssi(this)
  lazy val basicAuthSecurity = new BasicAuthSecurity(masterDatabase.db, config)
  lazy val localizationRepository = LocalizationRepository(config)
  lazy val oidGenerator = OidGenerator(config)
  lazy val env = Environment(this)

  lazy val init: Future[Unit] = {
    perustiedotIndexer.init // This one will not be awaited for; it's ok that indexing continues while application is running
    tryCatch("Koodistojen luonti") { if (config.getString("opintopolku.virkailija.url") != "mock") KoodistoCreator(this).createAndUpdateCodesBasedOnMockData }
    val parallels: immutable.Seq[Future[Any]] = List(
      Future { tiedonsiirtoService.init },
      Future { scheduledTasks.init },
      Future { localizationRepository.init }
    )

    Future.sequence(parallels).map(_ => ())
  }
}