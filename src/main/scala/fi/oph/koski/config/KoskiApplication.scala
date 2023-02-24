package fi.oph.koski.config

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.cache.CacheManager
import fi.oph.koski.db.{KoskiDatabase, RaportointiDatabaseConfig, RaportointiGenerointiDatabaseConfig, ValpasDatabaseConfig}
import fi.oph.koski.opensearch.{IndexManager, OpenSearch}
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.eperusteetvalidation.{EPerusteetFiller, EPerusteetOpiskeluoikeusChangeValidator, EPerusteisiinPerustuvaValidator}
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.healthcheck.HealthCheck
import fi.oph.koski.henkilo.{HenkilöRepository, Hetu, KoskiHenkilöCache, OpintopolkuHenkilöFacade}
import fi.oph.koski.history.{KoskiOpiskeluoikeusHistoryRepository, YtrOpiskeluoikeusHistoryRepository}
import fi.oph.koski.huoltaja.HuoltajaServiceVtj
import fi.oph.koski.koodisto.{KoodistoCreator, KoodistoPalvelu, KoodistoViitePalvelu}
import fi.oph.koski.koskiuser._
import fi.oph.koski.localization.{KoskiLocalizationConfig, LocalizationRepository}
import fi.oph.koski.log.{AuditLog, Logging, TimedProxy}
import fi.oph.koski.mydata.{MyDataRepository, MyDataService}
import fi.oph.koski.omattiedot.HuoltajaService
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.{OrganisaatioRepository, OrganisaatioService}
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedotIndexer, OpiskeluoikeudenPerustiedotRepository, PerustiedotManualSyncRepository, PerustiedotSyncRepository}
import fi.oph.koski.pulssi.{KoskiPulssi, PrometheusRepository}
import fi.oph.koski.raportointikanta.{Public, RaportointiDatabase, RaportointikantaService}
import fi.oph.koski.schedule.{KoskiScheduledTasks, PerustiedotManualSyncScheduler, PerustiedotSyncScheduler}
import fi.oph.koski.sso.{CasOppijaCreationService, CasService, KoskiSessionRepository}
import fi.oph.koski.suoritusjako.{SuoritusjakoRepository, SuoritusjakoRepositoryV2, SuoritusjakoService, SuoritusjakoServiceV2}
import fi.oph.koski.suostumus.SuostumuksenPeruutusService
import fi.oph.koski.tiedonsiirto.{IPService, TiedonsiirtoService}
import fi.oph.koski.tutkinto.TutkintoRepository
import fi.oph.koski.userdirectory.DirectoryClient
import fi.oph.koski.validation.{KoskiGlobaaliValidator, KoskiValidator, ValidatingAndResolvingExtractor}
import fi.oph.koski.valpas.db.ValpasDatabase
import fi.oph.koski.valpas.hakukooste.ValpasHakukoosteService
import fi.oph.koski.valpas.kansalainen.ValpasKansalainenService
import fi.oph.koski.valpas.kuntailmoitus.ValpasKuntailmoitusService
import fi.oph.koski.valpas.localization.ValpasLocalizationConfig
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasOpiskeluoikeusDatabaseService, ValpasRajapäivätService}
import fi.oph.koski.valpas.oppija.{ValpasOppijaLaajatTiedotService, ValpasOppijalistatService, ValpasOppijanumerorekisteriService}
import fi.oph.koski.valpas.oppijahaku.ValpasOppijaSearchService
import fi.oph.koski.valpas.oppivelvollisuudenkeskeytys.ValpasOppivelvollisuudenKeskeytysService
import fi.oph.koski.valpas.oppivelvollisuudestavapautus.ValpasOppivelvollisuudestaVapautusService
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaOppivelvollisuudenKeskeytysService
import fi.oph.koski.valpas.valpasrepository.{OpiskeluoikeusLisätiedotRepository, OppivelvollisuudenKeskeytysRepository, OppivelvollisuudenKeskeytysRepositoryService, ValpasKuntailmoitusRepository}
import fi.oph.koski.virta.{VirtaAccessChecker, VirtaClient, VirtaOpiskeluoikeusRepository}
import fi.oph.koski.ytr.download.YtrDownloadService
import fi.oph.koski.ytr.{YtrAccessChecker, YtrClient, YtrOpiskeluoikeusRepository, YtrRepository}

import scala.concurrent.Future

object KoskiApplication {
  lazy val defaultConfig = ConfigFactory.load

  def apply: KoskiApplication = apply(defaultConfig)

  def apply(config: Config): KoskiApplication = new KoskiApplication(config)
}

class KoskiApplication(
  val config: Config,
  implicit val cacheManager: CacheManager = new CacheManager
) extends Logging with UserAuthenticationContext with GlobalExecutionContext {

  lazy val casService = new CasService(config)
  lazy val casOppijaCreationService = new CasOppijaCreationService(henkilöRepository)
  lazy val organisaatioRepository = OrganisaatioRepository(config, koodistoViitePalvelu)
  lazy val organisaatioService = new OrganisaatioService(this)
  lazy val directoryClient = DirectoryClient(config, casService)
  lazy val ePerusteet = EPerusteetRepository.apply(config)
  lazy val tutkintoRepository = TutkintoRepository(ePerusteet, koodistoViitePalvelu)
  lazy val oppilaitosRepository = OppilaitosRepository(config, organisaatioRepository)
  lazy val koodistoPalvelu = KoodistoPalvelu.apply(config)
  lazy val koodistoViitePalvelu = KoodistoViitePalvelu(config, koodistoPalvelu)
  lazy val validatingAndResolvingExtractor = new ValidatingAndResolvingExtractor(koodistoViitePalvelu, organisaatioRepository)
  lazy val opintopolkuHenkilöFacade = OpintopolkuHenkilöFacade(config, masterDatabase.db, perustiedotRepository, perustiedotIndexer)
  lazy val käyttöoikeusRepository = new KäyttöoikeusRepository(organisaatioRepository, directoryClient)
  lazy val masterDatabase = KoskiDatabase.master(config)
  lazy val replicaDatabase = KoskiDatabase.replica(config)
  lazy val raportointiDatabase = new RaportointiDatabase(new RaportointiDatabaseConfig(config, schema = Public))
  lazy val raportointiGenerointiDatabase = new RaportointiDatabase(new RaportointiGenerointiDatabaseConfig(config, schema = Public))
  lazy val valpasDatabase = new ValpasDatabase(new ValpasDatabaseConfig(config))
  lazy val raportointikantaService = new RaportointikantaService(this)
  lazy val virtaClient = VirtaClient(config)
  lazy val ytrClient = YtrClient(config)
  lazy val ytrRepository = new YtrRepository(ytrClient)
  lazy val ytrDownloadService = new YtrDownloadService(masterDatabase.db, this)
  lazy val virtaAccessChecker = new VirtaAccessChecker(käyttöoikeusRepository)
  lazy val ytrAccessChecker = new YtrAccessChecker(käyttöoikeusRepository)
  lazy val henkilöRepository = HenkilöRepository(this)
  lazy val huoltajaService = new HuoltajaService(this)
  lazy val huoltajaServiceVtj = new HuoltajaServiceVtj(config, henkilöRepository)
  lazy val historyRepository = KoskiOpiskeluoikeusHistoryRepository(masterDatabase.db)
  lazy val ytrHistoryRepository = YtrOpiskeluoikeusHistoryRepository(masterDatabase.db)
  lazy val virta = TimedProxy[AuxiliaryOpiskeluoikeusRepository](VirtaOpiskeluoikeusRepository(virtaClient, oppilaitosRepository, koodistoViitePalvelu, organisaatioRepository, virtaAccessChecker, Some(validator)))
  lazy val henkilöCache = new KoskiHenkilöCache(masterDatabase.db)
  lazy val ePerusteetValidator = new EPerusteisiinPerustuvaValidator(ePerusteet, tutkintoRepository, koodistoViitePalvelu)
  lazy val ePerusteetChangeValidator = new EPerusteetOpiskeluoikeusChangeValidator(ePerusteet, tutkintoRepository, koodistoViitePalvelu)
  lazy val ePerusteetFiller = new EPerusteetFiller(ePerusteet, tutkintoRepository, koodistoViitePalvelu)
  lazy val possu = TimedProxy[KoskiOpiskeluoikeusRepository](new PostgresKoskiOpiskeluoikeusRepository(
    masterDatabase.db,
    historyRepository,
    henkilöCache,
    oidGenerator,
    henkilöRepository.opintopolku,
    perustiedotSyncRepository,
    organisaatioRepository,
    ePerusteetChangeValidator,
    config))
  lazy val possuV2 = TimedProxy[KoskiOpiskeluoikeusRepository](new PostgresKoskiOpiskeluoikeusRepositoryV2(
    masterDatabase.db,
    historyRepository,
    henkilöCache,
    oidGenerator,
    henkilöRepository.opintopolku,
    perustiedotSyncRepository,
    organisaatioRepository,
    ePerusteetChangeValidator,
    config))
  lazy val ytrPossu = TimedProxy[YtrSavedOpiskeluoikeusRepository](new PostgresYtrOpiskeluoikeusRepository(
    masterDatabase.db,
    ytrHistoryRepository,
    henkilöCache,
    oidGenerator,
    henkilöRepository.opintopolku))
  lazy val ytr = TimedProxy[AuxiliaryOpiskeluoikeusRepository](YtrOpiskeluoikeusRepository(ytrRepository, organisaatioRepository, oppilaitosRepository, koodistoViitePalvelu, ytrAccessChecker, Some(validator), koskiLocalizationRepository))
  lazy val opiskeluoikeusRepository = new CompositeOpiskeluoikeusRepository(possu, virta, ytr, hetu)
  lazy val opiskeluoikeusRepositoryV2 = new CompositeOpiskeluoikeusRepository(possuV2, virta, ytr, hetu)
  lazy val opiskeluoikeusQueryRepository = new OpiskeluoikeusQueryService(replicaDatabase.db)
  lazy val validator: KoskiValidator = new KoskiValidator(
    organisaatioRepository,
    possu,
    henkilöRepository,
    ePerusteetValidator,
    ePerusteetFiller,
    validatingAndResolvingExtractor,
    suostumuksenPeruutusService,
    koodistoViitePalvelu,
    config
  )
  lazy val openSearch = OpenSearch(config)
  lazy val perustiedotIndexer = new OpiskeluoikeudenPerustiedotIndexer(openSearch, opiskeluoikeusQueryRepository, perustiedotSyncRepository, perustiedotManualSyncRepository)
  lazy val perustiedotRepository = new OpiskeluoikeudenPerustiedotRepository(perustiedotIndexer, opiskeluoikeusQueryRepository)
  lazy val perustiedotSyncRepository = new PerustiedotSyncRepository(masterDatabase.db)
  lazy val perustiedotManualSyncRepository = new PerustiedotManualSyncRepository(masterDatabase.db, henkilöCache)
  lazy val perustiedotSyncScheduler = new PerustiedotSyncScheduler(this)
  lazy val perustiedotManualSyncScheduler = new PerustiedotManualSyncScheduler(this)
  lazy val oppijaFacade = new KoskiOppijaFacade(henkilöRepository, opiskeluoikeusRepository, ytrPossu, historyRepository, ytrHistoryRepository, globaaliValidator, config, hetu)
  lazy val oppijaFacadeV2 = new KoskiOppijaFacade(henkilöRepository, opiskeluoikeusRepositoryV2, ytrPossu, historyRepository, ytrHistoryRepository, globaaliValidatorV2, config, hetu)
  lazy val suoritusjakoRepository = new SuoritusjakoRepository(masterDatabase.db)
  lazy val suoritusjakoService = new SuoritusjakoService(suoritusjakoRepository, oppijaFacade)
  lazy val suoritusjakoRepositoryV2 = new SuoritusjakoRepositoryV2(masterDatabase.db)
  lazy val suoritusjakoServiceV2 = new SuoritusjakoServiceV2(suoritusjakoRepositoryV2, henkilöRepository, opiskeluoikeusRepositoryV2, this)
  lazy val mydataRepository = new MyDataRepository(masterDatabase.db)
  lazy val mydataService = new MyDataService(mydataRepository, this)
  lazy val sessionTimeout = SessionTimeout(config)
  lazy val koskiSessionRepository = new KoskiSessionRepository(masterDatabase.db, sessionTimeout)
  lazy val fixtureCreator = new FixtureCreator(this)
  lazy val tiedonsiirtoService = new TiedonsiirtoService(openSearch, organisaatioRepository, henkilöRepository, koodistoViitePalvelu, hetu)
  lazy val healthCheck = HealthCheck(this)
  lazy val scheduledTasks = new KoskiScheduledTasks(this)
  lazy val ipService = new IPService(masterDatabase.db)
  lazy val prometheusRepository = PrometheusRepository(config)
  lazy val koskiPulssi = KoskiPulssi(this)
  lazy val koskiLocalizationRepository = LocalizationRepository(config, new KoskiLocalizationConfig)
  lazy val päivitetytOpiskeluoikeudetJono = new PäivitetytOpiskeluoikeudetJonoService(this)
  lazy val valpasLocalizationRepository = LocalizationRepository(config, new ValpasLocalizationConfig)
  lazy val valpasRajapäivätService = ValpasRajapäivätService(config)
  lazy val valpasOppijaLaajatTiedotService = new ValpasOppijaLaajatTiedotService(this)
  lazy val valpasOppijalistatService = new ValpasOppijalistatService(this)
  lazy val valpasHakukoosteService = ValpasHakukoosteService(this)
  lazy val valpasKansalainenService = new ValpasKansalainenService(this)
  lazy val valpasOppijanumerorekisteriService = new ValpasOppijanumerorekisteriService(this)
  lazy val valpasOppijaSearchService = new ValpasOppijaSearchService(this)
  lazy val valpasKuntailmoitusRepository = new ValpasKuntailmoitusRepository(
    valpasDatabase, validatingAndResolvingExtractor, valpasRajapäivätService, config
  )
  lazy val valpasOppivelvollisuudenKeskeytysRepositoryService = new OppivelvollisuudenKeskeytysRepositoryService(this)
  lazy val valpasOppivelvollisuudenKeskeytysService = new ValpasOppivelvollisuudenKeskeytysService(this)
  lazy val valpasOpiskeluoikeusLisätiedotRepository = new OpiskeluoikeusLisätiedotRepository(valpasDatabase, config)
  lazy val valpasKuntailmoitusService = new ValpasKuntailmoitusService(this)
  lazy val valpasOppivelvollisuudenKeskeytysRepository = new OppivelvollisuudenKeskeytysRepository(valpasDatabase, config)
  lazy val valpasOpiskeluoikeusDatabaseService = new ValpasOpiskeluoikeusDatabaseService(this)
  lazy val valpasRouhintaOppivelvollisuudenKeskeytysService = new ValpasRouhintaOppivelvollisuudenKeskeytysService(this)
  lazy val valpasOppivelvollisuudestaVapautusService = new ValpasOppivelvollisuudestaVapautusService(this)
  lazy val oidGenerator = OidGenerator(config)
  lazy val hetu = new Hetu(config.getBoolean("acceptSyntheticHetus"))
  lazy val indexManager = new IndexManager(List(perustiedotIndexer.index, tiedonsiirtoService.index))
  lazy val suostumuksenPeruutusService = SuostumuksenPeruutusService(this)
  lazy val globaaliValidator: KoskiGlobaaliValidator = new KoskiGlobaaliValidator(
    opiskeluoikeusRepository,
    valpasRajapäivätService,
    raportointiDatabase,
  )
  lazy val globaaliValidatorV2: KoskiGlobaaliValidator = new KoskiGlobaaliValidator(
    opiskeluoikeusRepositoryV2,
    valpasRajapäivätService,
    raportointiDatabase,
  )

  def init(): Future[Any] = {
    AuditLog.startHeartbeat()

    tryCatch("Koodistojen luonti") {
      if (config.getString("opintopolku.virkailija.url") != "mock") {
        KoodistoCreator(this).createAndUpdateCodesBasedOnMockData
      }
    }

    val parallels: Seq[Future[Any]] = Seq(
      Future(perustiedotIndexer.init()),
      Future(tiedonsiirtoService.init()),
      Future(koskiLocalizationRepository.init),
      Future(valpasLocalizationRepository.init)
    )

    // Init scheduled tasks only after ES indexes have been initialized:
    Future.sequence(parallels).map(_ => Future(scheduledTasks.init))
  }
}
