package fi.oph.koski.raportointikanta

import fi.oph.koski.cloudwatch.CloudWatchMetricsService
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.RaportointiDatabaseConfig
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import org.apache.log4j.LogManager
import rx.lang.scala.schedulers.NewThreadScheduler
import rx.lang.scala.{Observable, Scheduler}

import scala.language.postfixOps

class RaportointikantaService(application: KoskiApplication) extends Logging {
  private val cloudWatchMetrics = CloudWatchMetricsService.apply(application.config)
  private val eventBridgeClient = KoskiEventBridgeClient.apply(application.config)

  def loadRaportointikanta(force: Boolean, scheduler: Scheduler = defaultScheduler, onEnd: () => Unit = () => ()): Boolean = {
    if (isLoading && !force) {
      logger.info("Raportointikanta already loading, do nothing")
      false
    } else {
      loadDatabase.dropAndCreateObjects
      startLoading(scheduler, onEnd)
      logger.info(s"Started loading raportointikanta (force: $force)")
      true
    }
  }

  def loadRaportointikantaAndExit(): Unit = {
    loadRaportointikanta(force = true, defaultScheduler, onEnd = () => {
      logger.info(s"Ended loading raportointikanta, shutting down...")
      shutdown
    })
  }

  private def loadOpiskeluoikeudet(db: RaportointiDatabase): Observable[LoadResult] = {
    // Ensure that nobody uses koskiSession implicitely
    implicit val systemUser = KoskiSpecificSession.systemUser
    OpiskeluoikeusLoader.loadOpiskeluoikeudet(application.opiskeluoikeusQueryRepository, systemUser, db)
  }

  def loadHenkilöt(db: RaportointiDatabase = raportointiDatabase): Int =
    HenkilöLoader.loadHenkilöt(application.opintopolkuHenkilöFacade, db, application.koodistoPalvelu)

  def loadOrganisaatiot(db: RaportointiDatabase = raportointiDatabase): Int =
    OrganisaatioLoader.loadOrganisaatiot(application.organisaatioRepository, db)

  def loadKoodistot(db: RaportointiDatabase = raportointiDatabase): Int =
    KoodistoLoader.loadKoodistot(application.koodistoPalvelu, db)

  def isLoading: Boolean = loadDatabase.status.isLoading

  def isAvailable: Boolean = raportointiDatabase.status.isComplete
  def isLoadComplete: Boolean = !isLoading && isAvailable
  def isEmpty: Boolean = raportointiDatabase.status.isEmpty

  def status: Map[String, RaportointikantaStatusResponse] =
    List(loadDatabase.status, raportointiDatabase.status).groupBy(_.schema).mapValues(_.head)

  def putUploadEvents(): Unit = {
    eventBridgeClient.putEvents(
      EventBridgeEvent(raportointikantaGeneration, Map("event" -> "start-upload", "uploadTarget" -> "lampi")),
      EventBridgeEvent(raportointikantaGeneration, Map("event" -> "start-upload", "uploadTarget" -> "csc"))
    )
  }

  def putLoadTimeMetric(): Unit = {
    (raportointiDatabase.status.startedTime, raportointiDatabase.status.completionTime) match {
      case (Some(started), Some(completed)) => cloudWatchMetrics.putRaportointikantaLoadtime(started, completed)
      case _ => logger.info("Cannot put raportointikanta load time metric: load start or end time missing (probably never loaded yet?)")
    }
  }

  private def startLoading(scheduler: Scheduler, onEnd: () => Unit) = {
    logger.info(s"Start loading raportointikanta into ${loadDatabase.schema.name}")
    loadOpiskeluoikeudet(loadDatabase)
      .subscribeOn(scheduler)
      .subscribe(
        onNext = doNothing,
        onError = (_) => onEnd(),
        onCompleted = () => {
          //Without try-catch, in case of an exception the process just silently halts, this is a feature of java.util.concurrent.Executors
          try {
            loadRestAndSwap()
            putUploadEvents()
            putLoadTimeMetric()
            onEnd()
          } catch {
            case e: Throwable => {
              logger.error(e)(e.toString)
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
    loadDatabase.createCustomFunctions
    loadDatabase.createMaterializedViews
    swapRaportointikanta()
    raportointiDatabase.vacuumAnalyze()
  }

  protected lazy val defaultScheduler: Scheduler = NewThreadScheduler()

  private def swapRaportointikanta(): Unit = raportointiDatabase.dropPublicAndMoveTempToPublic

  private lazy val loadDatabase = new RaportointiDatabase(new RaportointiDatabaseConfig(application.config, schema = Temp))
  private lazy val raportointiDatabase = application.raportointiDatabase

  private val raportointikantaGeneration = "raportointikanta-generation"

  private def shutdown = {
    LogManager.shutdown()
    sys.exit()
  }
}
