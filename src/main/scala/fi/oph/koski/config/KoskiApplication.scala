package fi.oph.koski.config

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.cache.CacheManager
import fi.oph.koski.db._
import fi.oph.koski.elasticsearch.{ElasticSearch, IndexManager}
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.healthcheck.HealthCheck
import fi.oph.koski.henkilo.{HenkilöRepository, Hetu, KoskiHenkilöCache, OpintopolkuHenkilöFacade}
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.huoltaja.HuoltajaServiceVtj
import fi.oph.koski.koodisto.{KoodistoCreator, KoodistoPalvelu, KoodistoViitePalvelu}
import fi.oph.koski.koskiuser._
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.common.log.{AuditLog, Logging, TimedProxy}
import fi.oph.koski.mydata.{MyDataRepository, MyDataService}
import fi.oph.koski.omattiedot.HuoltajaService
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.{OrganisaatioRepository, OrganisaatioService}
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedotIndexer, OpiskeluoikeudenPerustiedotRepository, PerustiedotSyncRepository}
import fi.oph.koski.pulssi.{KoskiPulssi, PrometheusRepository}
import fi.oph.koski.raportointikanta.{Public, RaportointiDatabase, RaportointikantaService}
import fi.oph.koski.schedule.{KoskiScheduledTasks, PerustiedotSyncScheduler}
import fi.oph.koski.sso.KoskiSessionRepository
import fi.oph.koski.suoritusjako.{SuoritusjakoRepository, SuoritusjakoRepositoryV2, SuoritusjakoService, SuoritusjakoServiceV2}
import fi.oph.koski.tiedonsiirto.{IPService, TiedonsiirtoService}
import fi.oph.koski.tutkinto.TutkintoRepository
import fi.oph.koski.userdirectory.DirectoryClient
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.virta.{VirtaAccessChecker, VirtaClient, VirtaOpiskeluoikeusRepository}
import fi.oph.koski.ytr.{YtrAccessChecker, YtrClient, YtrOpiskeluoikeusRepository, YtrRepository}

import scala.collection.immutable
import scala.concurrent.Future

object KoskiApplication {
  lazy val defaultConfig = ConfigFactory.load

  def apply: KoskiApplication = apply(defaultConfig)

  def apply(config: Config): KoskiApplication = new KoskiApplication(config)
}

class KoskiApplication(val config: Config, implicit val cacheManager: CacheManager = new CacheManager) extends Logging with UserAuthenticationContext with GlobalExecutionContext {
  lazy val runMode = RunMode.get
  lazy val organisaatioRepository = OrganisaatioRepository(config, koodistoViitePalvelu)
  lazy val organisaatioService = new OrganisaatioService(this)
  lazy val directoryClient = DirectoryClient(config)
  lazy val ePerusteet = EPerusteetRepository.apply(config)
  lazy val tutkintoRepository = TutkintoRepository(ePerusteet, koodistoViitePalvelu)
  lazy val oppilaitosRepository = new OppilaitosRepository(organisaatioRepository)
  lazy val koodistoPalvelu = KoodistoPalvelu.apply(config)
  lazy val koodistoViitePalvelu = KoodistoViitePalvelu(koodistoPalvelu)
  lazy val opintopolkuHenkilöFacade = OpintopolkuHenkilöFacade(config, masterDatabase.db, perustiedotRepository, perustiedotIndexer)
  lazy val käyttöoikeusRepository = new KäyttöoikeusRepository(organisaatioRepository, directoryClient)
  lazy val masterDatabase = KoskiDatabase.master(config)
  lazy val replicaDatabase = KoskiDatabase.replica(config, masterDatabase)
  lazy val raportointiConfig = KoskiDatabaseConfig(config, raportointiSchema = Some(Public))
  lazy val raportointiDatabase = new RaportointiDatabase(raportointiConfig)
  lazy val raportointikantaService = new RaportointikantaService(this)
  lazy val virtaClient = VirtaClient(config)
  lazy val ytrClient = YtrClient(config)
  lazy val ytrRepository = new YtrRepository(ytrClient)
  lazy val virtaAccessChecker = new VirtaAccessChecker(käyttöoikeusRepository)
  lazy val ytrAccessChecker = new YtrAccessChecker(käyttöoikeusRepository)
  lazy val henkilöRepository = HenkilöRepository(this)
  lazy val huoltajaService = new HuoltajaService(this)
  lazy val huoltajaServiceVtj = new HuoltajaServiceVtj(config, henkilöRepository)
  lazy val historyRepository = OpiskeluoikeusHistoryRepository(masterDatabase.db)
  lazy val virta = TimedProxy[AuxiliaryOpiskeluoikeusRepository](VirtaOpiskeluoikeusRepository(virtaClient, oppilaitosRepository, koodistoViitePalvelu, organisaatioRepository, virtaAccessChecker, Some(validator)))
  lazy val henkilöCache = new KoskiHenkilöCache(masterDatabase.db)
  lazy val possu = TimedProxy[KoskiOpiskeluoikeusRepository](new PostgresOpiskeluoikeusRepository(masterDatabase.db, historyRepository, henkilöCache, oidGenerator, henkilöRepository.opintopolku, perustiedotSyncRepository))
  lazy val possuV2 = TimedProxy[KoskiOpiskeluoikeusRepository](new PostgresOpiskeluoikeusRepositoryV2(masterDatabase.db, historyRepository, henkilöCache, oidGenerator, henkilöRepository.opintopolku, perustiedotSyncRepository))
  lazy val ytr = TimedProxy[AuxiliaryOpiskeluoikeusRepository](YtrOpiskeluoikeusRepository(ytrRepository, organisaatioRepository, oppilaitosRepository, koodistoViitePalvelu, ytrAccessChecker, Some(validator), localizationRepository))
  lazy val opiskeluoikeusRepository = new CompositeOpiskeluoikeusRepository(possu, virta, ytr)
  lazy val opiskeluoikeusRepositoryV2 = new CompositeOpiskeluoikeusRepository(possuV2, virta, ytr)
  lazy val opiskeluoikeusQueryRepository = new OpiskeluoikeusQueryService(replicaDatabase.db)
  lazy val validator: KoskiValidator = new KoskiValidator(tutkintoRepository, koodistoViitePalvelu, organisaatioRepository, possu, henkilöRepository, ePerusteet, config)
  lazy val elasticSearch = ElasticSearch(config)
  lazy val perustiedotIndexer = new OpiskeluoikeudenPerustiedotIndexer(elasticSearch, opiskeluoikeusQueryRepository, perustiedotSyncRepository)
  lazy val perustiedotRepository = new OpiskeluoikeudenPerustiedotRepository(perustiedotIndexer, opiskeluoikeusQueryRepository)
  lazy val perustiedotSyncRepository = new PerustiedotSyncRepository(masterDatabase.db)
  lazy val perustiedotSyncScheduler = new PerustiedotSyncScheduler(this)
  lazy val oppijaFacade = new KoskiOppijaFacade(henkilöRepository, henkilöCache, opiskeluoikeusRepository, historyRepository, perustiedotIndexer, config, hetu)
  lazy val oppijaFacadeV2 = new KoskiOppijaFacade(henkilöRepository, henkilöCache, opiskeluoikeusRepositoryV2, historyRepository, perustiedotIndexer, config, hetu)
  lazy val suoritusjakoRepository = new SuoritusjakoRepository(masterDatabase.db)
  lazy val suoritusjakoService = new SuoritusjakoService(suoritusjakoRepository, oppijaFacade)
  lazy val suoritusjakoRepositoryV2 = new SuoritusjakoRepositoryV2(masterDatabase.db)
  lazy val suoritusjakoServiceV2 = new SuoritusjakoServiceV2(suoritusjakoRepositoryV2, henkilöRepository, opiskeluoikeusRepositoryV2, this)
  lazy val mydataRepository = new MyDataRepository(masterDatabase.db)
  lazy val mydataService = new MyDataService(mydataRepository, this)
  lazy val sessionTimeout = SessionTimeout(config)
  lazy val koskiSessionRepository = new KoskiSessionRepository(masterDatabase.db, sessionTimeout)
  lazy val fixtureCreator = new FixtureCreator(this)
  lazy val tiedonsiirtoService = new TiedonsiirtoService(elasticSearch, organisaatioRepository, henkilöRepository, koodistoViitePalvelu, hetu)
  lazy val healthCheck = HealthCheck(this)
  lazy val scheduledTasks = new KoskiScheduledTasks(this)
  lazy val ipService = new IPService(masterDatabase.db)
  lazy val prometheusRepository = PrometheusRepository(config)
  lazy val koskiPulssi = KoskiPulssi(this)
  lazy val localizationRepository = LocalizationRepository(config)
  lazy val oidGenerator = OidGenerator(config)
  lazy val hetu = new Hetu(config.getBoolean("acceptSyntheticHetus"))
  lazy val features = Features(config)
  lazy val indexManager = new IndexManager(List(perustiedotIndexer.index, tiedonsiirtoService.index))

  def init(): Future[Any] = {
    AuditLog.startHeartbeat()

    tryCatch("Koodistojen luonti") {
      if (config.getString("opintopolku.virkailija.url") != "mock") {
        KoodistoCreator(this).createAndUpdateCodesBasedOnMockData
      }
    }

    val parallels: immutable.Seq[Future[Any]] = List(
      Future(perustiedotIndexer.init()),
      Future(tiedonsiirtoService.init()),
      Future(localizationRepository.init)
    )
    // Init scheduled tasks only after ES indexes have been initialized:
    Future.sequence(parallels).map(_ => Future(scheduledTasks.init))
  }
}
