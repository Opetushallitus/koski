package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import rx.lang.scala.schedulers.NewThreadScheduler
import rx.lang.scala.{Observable, Scheduler}

import scala.language.postfixOps

class RaportointikantaService(application: KoskiApplication) extends Logging {
  def dropAndCreateSchema() {
    logger.info("Clearing raportointikanta...")
    raportointiDatabase.dropAndCreateObjects
  }

  def loadRaportointikanta(force: Boolean, scheduler: Scheduler = defaultScheduler): Boolean = if (!force && isLoading) {
    logger.info("Raportointikanta already loading, do nothing")
    false
  } else {
    loadDatabase.dropAndCreateObjects
    startLoading(scheduler)
    logger.info(s"Started loading raportointikanta (force: $force)")
    true
  }

  def loadOpiskeluoikeudet(db: RaportointiDatabase = raportointiDatabase): Observable[LoadResult] = {
    // Ensure that nobody uses koskiSession implicitely
    implicit val systemUser = KoskiSession.systemUser
    OpiskeluoikeusLoader.loadOpiskeluoikeudet(application.opiskeluoikeusQueryRepository, systemUser, db)
  }

  def loadHenkilöt(db: RaportointiDatabase = raportointiDatabase): Int =
    HenkilöLoader.loadHenkilöt(application.opintopolkuHenkilöFacade, db)

  def loadOrganisaatiot(db: RaportointiDatabase = raportointiDatabase): Int =
    OrganisaatioLoader.loadOrganisaatiot(application.organisaatioRepository, db)

  def loadKoodistot(db: RaportointiDatabase = raportointiDatabase): Int =
    KoodistoLoader.loadKoodistot(application.koodistoPalvelu, db)

  def isLoading: Boolean = loadDatabase.status.isLoading

  def isAvailable: Boolean = raportointiDatabase.status.isComplete
  def isLoadComplete: Boolean = !isLoading && isAvailable

  def status: Map[String, RaportointikantaStatusResponse] =
    List(loadDatabase.status, raportointiDatabase.status).groupBy(_.schema).mapValues(_.head)

  private def startLoading(scheduler: Scheduler): Unit = {
    logger.info(s"Start loading raportointikanta into ${loadDatabase.schema.name}")
    loadOpiskeluoikeudet(loadDatabase)
     .subscribeOn(scheduler)
     .subscribe(
        onNext = doNothing,
        onError = doNothing,
        onCompleted = loadRestAndSwap
      )
  }

  private val doNothing = (_: Any) => ()

  private val loadRestAndSwap = () => {
    loadHenkilöt(loadDatabase)
    loadOrganisaatiot(loadDatabase)
    loadKoodistot(loadDatabase)
    swapRaportointikanta()
  }

  protected lazy val defaultScheduler: Scheduler = NewThreadScheduler()

  private def swapRaportointikanta() {
    raportointiDatabase.moveTo(Old)
    loadDatabase.moveTo(raportointiDatabase.schema)
  }

  private lazy val loadDatabase = new RaportointiDatabase(application.raportointiConfig, Temp)
  private lazy val raportointiDatabase = application.raportointiDatabase
}
