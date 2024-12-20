package fi.oph.koski.raportointikanta

import fi.oph.koski.cloudwatch.CloudWatchMetricsService
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.{OpiskeluoikeusRow, RaportointiGenerointiDatabaseConfig}
import fi.oph.koski.henkilo.{OpintopolkuHenkilöFacade, OppijanumeroRekisteriClientRetryStrategy}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import rx.lang.scala.schedulers.NewThreadScheduler
import rx.lang.scala.{Observable, Scheduler}

import java.time.ZonedDateTime
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

class RaportointikantaService(application: KoskiApplication) extends Logging {
  private val cloudWatchMetrics = CloudWatchMetricsService.apply(application.config)
  private val eventBridgeClient = KoskiEventBridgeClient.apply(application.config)
  private val päivitetytOpiskeluoikeudetJonoService = application.päivitetytOpiskeluoikeudetJono

  def loadRaportointikanta(
    force: Boolean,
    scheduler: Scheduler = defaultScheduler,
    onEnd: () => Unit = () => (),
    pageSize: Int = OpiskeluoikeusLoader.DefaultBatchSize,
    onAfterPage: (Int, Seq[OpiskeluoikeusRow]) => Unit = (_, _) => (),
    skipUnchangedData: Boolean = false,
    enableYtr: Boolean = true,
    incrementalLoadMaxRows: Int = 50000
  ): Boolean = {
    if (isLoading && !force) {
      logger.info("Raportointikanta already loading, do nothing")
      onEnd()
      false
    } else {
      val update = if (skipUnchangedData) {
        val dueTime = getDueTime
        val updatesInQueue = päivitetytOpiskeluoikeudetJonoService.päivitetytOpiskeluoikeudetCount(dueTime)

        if(incrementalLoadMaxRows <= updatesInQueue) {
          logger.warn(s"Raportointikanta incremental loading overridden with full reload: updates in queue: $updatesInQueue, incremental load threshold: $incrementalLoadMaxRows")
          None
        } else {
          Some(RaportointiDatabaseUpdate(
            previousRaportointiDatabase = raportointiDatabase,
            readReplicaDb = application.replicaDatabase.db,
            dueTime = dueTime,
            sleepDuration = sleepDuration,
            service = päivitetytOpiskeluoikeudetJonoService,
          ))
        }
      } else {
        None
      }
      loadDatabase.dropAndCreateObjects(Some(raportointiDatabase.status.dataVersion.getOrElse(0)))
      startLoading(update, enableYtr, scheduler, onEnd, pageSize, onAfterPage)
      logger.info(s"Started loading raportointikanta (force: $force, duetime: ${update.map(_.dueTime.toString).getOrElse("-")})")
      true
    }
  }

  def loadRaportointikantaAndExit(fullReload: Boolean, forceReload: Boolean, enableYtr: Boolean, incrementalLoadMaxRows: Int): Unit = {
    val skipUnchangedData = !fullReload
    loadRaportointikanta(
      force = forceReload,
      skipUnchangedData = skipUnchangedData,
      enableYtr = enableYtr,
      scheduler = defaultScheduler,
      onEnd = () => {
        logger.info(s"Ended loading raportointikanta, shutting down...")
        shutdown
      },
      incrementalLoadMaxRows = incrementalLoadMaxRows
    )
  }

  private def loadOpiskeluoikeudet(
    db: RaportointiDatabase,
    update: Option[RaportointiDatabaseUpdate],
    enableYtr: Boolean,
    pageSize: Int,
    onAfterPage: (Int, Seq[OpiskeluoikeusRow]) => Unit
  ): Observable[LoadResult] = {
    // Ensure that nobody uses koskiSession implicitely
    implicit val systemUser = KoskiSpecificSession.systemUser

    val loader = update match {
      case Some(update) => new IncrementalUpdateOpiskeluoikeusLoader(
        application.suostumuksenPeruutusService,
        application.organisaatioRepository,
        db,
        enableYtr,
        update,
        pageSize,
        onAfterPage,
      )
      case _ => new FullReloadOpiskeluoikeusLoader(
        application.opiskeluoikeusQueryRepository,
        application.suostumuksenPeruutusService,
        application.organisaatioRepository,
        db,
        enableYtr,
        pageSize,
        onAfterPage,
      )
    }

    loader.loadOpiskeluoikeudet()
  }

  def loadHenkilöt(db: RaportointiDatabase = raportointiDatabase): Int = {
    val kuntakoodit = application.koodistoPalvelu.getLatestVersionOptional("kunta")
      .map(application.koodistoPalvelu.getKoodistoKoodit)
      .getOrElse(List.empty)
    // Luodaan raportointikannan generointia varten oppijanumerorekisteriin oma client, jolla on pitkähermoinen uudelleenyritysstrategia
    val opintopolkuHenkilöFacade = application.opintopolkuHenkilöFacade.withRetryStrategy(
      OppijanumeroRekisteriClientRetryStrategy(
        maxRetries = 10,
        maxWaitBetweenRetries = 5.minutes,
        retryTimeout = 3.minutes,
      )
    )
    HenkilöLoader.loadHenkilöt(opintopolkuHenkilöFacade, db, application.koodistoPalvelu, application.opiskeluoikeusRepository, kuntakoodit)
  }

  def loadOrganisaatiot(db: RaportointiDatabase = raportointiDatabase): Int =
    OrganisaatioLoader.loadOrganisaatiot(application.organisaatioRepository, db)

  def loadKoodistot(db: RaportointiDatabase = raportointiDatabase): Int =
    KoodistoLoader.loadKoodistot(application.koodistoPalvelu, db)

  def loadOppivelvollisuudestaVapautukset(db: RaportointiDatabase = raportointiDatabase): Int =
    OppivelvollisuudenVapautusLoader.loadOppivelvollisuudestaVapautukset(application.valpasOppivelvollisuudestaVapautusService, db)

  def isLoading: Boolean =
    loadDatabase.status.isLoading && {
      if (Environment.isServerEnvironment(application.config)) {
        application.ecsMetadata.taskARN.exists { thisTask =>
          val loaderTasks = application.ecsMetadata.currentlyRunningRaportointikantaLoaderInstances
          val otherLoaderTasks = loaderTasks.filterNot(_.taskArn == thisTask)
          val loading = otherLoaderTasks.nonEmpty
          if (!loading) {
            logger.warn("Raportointikannan generointi oli tietokannan mukaan käynnissä, mutta yhtäkään hengissä olevaa instanssia ei löytynyt. Tämä viittaa siihen että edellinen generointi on kaatunut tai pysäytetty kesken.")
          }
          loading
        }
      } else {
        true
      }
    }


  def isAvailable: Boolean = raportointiDatabase.status.isComplete
  def isLoadComplete: Boolean = !isLoading && isAvailable
  def isEmpty: Boolean = raportointiDatabase.status.isEmpty

  def status: Map[String, RaportointikantaStatusResponse] =
    List(loadDatabase.status, raportointiDatabase.status).groupBy(_.schema).mapValues(_.head)

  def putUploadEvents(): Unit = {
    eventBridgeClient.putEvents(
      EventBridgeEvent(tietokantaUpload, Map("event" -> "start-upload", "uploadTarget" -> "LampiRaportointikanta")),
      EventBridgeEvent(tietokantaUpload, Map("event" -> "start-upload", "uploadTarget" -> "LampiRaportointikantaConfidential")),
      EventBridgeEvent(tietokantaUpload, Map("event" -> "start-upload", "uploadTarget" -> "LampiValpas")),
      EventBridgeEvent(tietokantaUpload, Map("event" -> "start-upload", "uploadTarget" -> "VipunenRaportointikanta")),
      EventBridgeEvent(tietokantaUpload, Map("event" -> "start-upload", "uploadTarget" -> "VipunenValpas"))
    )
    logger.info("Successfully sent start-upload events.")
  }

  def putLoadTimeMetric(succeeded: Option[Boolean]): Unit = {
    (raportointiDatabase.status.startedTime, raportointiDatabase.status.completionTime) match {
      case (Some(started), Some(completed)) => {
        cloudWatchMetrics.putRaportointikantaLoadtime(started, completed, succeeded)
        logger.info("Successfully sent load time data to Cloudwatch Metrics.")
      }
      case _ => logger.info("Cannot put raportointikanta load time metric: load start or end time missing (probably never loaded yet?)")
    }
  }

  private def startLoading(
    update: Option[RaportointiDatabaseUpdate],
    enableYtr: Boolean,
    scheduler: Scheduler,
    onEnd: () => Unit,
    pageSize: Int,
    onAfterPage: (Int, Seq[OpiskeluoikeusRow]) => Unit
  ) = {
    val startTime = ZonedDateTime.now()
    update match {
      case Some(u) => logger.info(s"Start updating raportointikanta in ${loadDatabase.schema.name} with new opiskeluoikeudet until ${u.dueTime} from ${u.previousRaportointiDatabase.schema.name}")
      case None => logger.info(s"Start loading raportointikanta into ${loadDatabase.schema.name} (full reload)")
    }
    putLoadTimeMetric(None) // To store metric more often, Cloudwatch metric does not support missing data more than 24 hours
    loadOpiskeluoikeudet(
      loadDatabase,
      update,
      enableYtr,
      pageSize,
      onAfterPage,
    )
      .subscribeOn(scheduler)
      .subscribe(
        onNext = doNothing,
        onError = (_) => {
          putLoadTimeMetric(Option(false))
          onEnd()
        },
        onCompleted = () => {
          //Without try-catch, in case of an exception the process just silently halts, this is a feature of java.util.concurrent.Executors
          try {
            loadRestAndSwap()
            if (update.isDefined) {
              päivitetytOpiskeluoikeudetJonoService.poistaKäsitellyt()
            } else {
              päivitetytOpiskeluoikeudetJonoService.poistaVanhat(startTime)
            }
            putUploadEvents()
            putLoadTimeMetric(Option(true))
            onEnd()
          } catch {
            case e: Throwable => {
              logger.error(e)(e.toString)
              putLoadTimeMetric(Option(false))
              onEnd()
            }
          }
        }
      )
  }

  private val doNothing = (_: Any) => ()

  private val loadRestAndSwap = () => {
    loadHenkilöt(loadDatabase)
    loadOrganisaatiot(loadDatabase)
    loadKoodistot(loadDatabase)
    loadOppivelvollisuudestaVapautukset(loadDatabase)
    loadDatabase.createOtherIndexes()
    loadDatabase.createCustomFunctions()
    loadDatabase.createPrecomputedTables(application.valpasRajapäivätService)
    swapRaportointikanta()
    raportointiDatabase.vacuumAnalyze()
  }

  private def getDueTime: ZonedDateTime =
    Environment.raportointikantaLoadDueTime.getOrElse(
      if (isMockEnvironment) ZonedDateTime.now().plusSeconds(5) else nextMidnight
    )

  private def nextMidnight: ZonedDateTime = {
    val now = ZonedDateTime.now()
    ZonedDateTime.of(
      now.getYear, now.getMonthValue, now.getDayOfMonth,
      0, 0, 0, 0,
      now.getZone
    ).plusDays(1)
  }

  private val sleepDuration: FiniteDuration =
    if (isMockEnvironment) 1.seconds else 5.minutes

  protected lazy val defaultScheduler: Scheduler = NewThreadScheduler()

  private def swapRaportointikanta(): Unit = raportointiDatabase.dropPublicAndMoveTempToPublic

  private lazy val loadDatabase = new RaportointiDatabase(new RaportointiGenerointiDatabaseConfig(application.config, schema = Temp))
  private lazy val raportointiDatabase = application.raportointiGenerointiDatabase

  private val tietokantaUpload = "database-upload"

  protected lazy val isMockEnvironment: Boolean = Environment.isMockEnvironment(application.config)

  def shutdown = {
    Thread.sleep(60000) //Varmistetaan, että kaikki logit ehtivät varmasti siirtyä Cloudwatchiin ennen sulkemista.
    sys.exit()
  }
}
