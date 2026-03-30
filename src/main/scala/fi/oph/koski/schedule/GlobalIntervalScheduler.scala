package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis
import java.sql.Timestamp
import java.time.{Duration, LocalDateTime}
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.executors.NamedThreadFactory
import fi.oph.koski.log.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods

/** Lease-kontrolloitu scheduler, jossa kaikki lease-haltijat jakavat saman aikataulun DB:n kautta.
 *
 * Jos concurrency >= 2, niin useampi lease-haltija voi suorittaa tehtävää yhtäaikaa, mutta ne jakavat
 * silti yhteisen nextFireTime -kentän tietokannassa, mikä vähentää suorituskertoja.
 *
 * HUOM! nextFireTime:n rinnakkaista lukemista ja asettamista ei ole tietokantalukoilla tällä hetkellä
 * estetty, joten rinnakkaisten suoritusten määrä ei ole täsmälleen rajoitettu.
 * */
object GlobalIntervalScheduler {
  def apply(
    application: KoskiApplication,
    name: String,
    interval: Duration,
    task: () => Unit,
    shouldFireCheckIntervalMillis: Int,
    concurrency: Int // Lease-slottien määrä. Arvot <= 0 toimivat kuten 1.
  ): GlobalIntervalScheduler =
    new GlobalIntervalScheduler(
      application,
      name,
      interval,
      _ => { task(); None },
      shouldFireCheckIntervalMillis,
      concurrency,
      initialContext = None,
      readAndWriteContext = false,
      leaseElectorOverrideForTests = None
    )

  def withLeaseElectorOverrideForTests(
    application: KoskiApplication,
    name: String,
    interval: Duration,
    task: () => Unit,
    shouldFireCheckIntervalMillis: Int,
    concurrency: Int, // Lease-slottien määrä. Arvot <= 0 toimivat kuten 1.
    leaseElectorOverrideForTests: WorkerLeaseElector
  ): GlobalIntervalScheduler = {
    require(Environment.isUnitTestEnvironment(application.config), "leaseElectorOverrideForTests sallittu vain testeissä")

    new GlobalIntervalScheduler(
      application,
      name,
      interval,
      _ => { task(); None },
      shouldFireCheckIntervalMillis,
      concurrency,
      initialContext = None,
      readAndWriteContext = false,
      leaseElectorOverrideForTests = Some(leaseElectorOverrideForTests)
    )
  }

  /** Luo lease-kontrolloidun schedulerin kontekstin kanssa. Context toimii oikein vain yhdellä nodella,
   * joten tämä pakottaa concurrency = 1. */
  def withContext(
    application: KoskiApplication,
    name: String,
    interval: Duration,
    initialContext: JValue,
    task: Option[JValue] => Option[JValue],
    shouldFireCheckIntervalMillis: Int
  ): GlobalIntervalScheduler =
    new GlobalIntervalScheduler(
      application,
      name,
      interval,
      initialContext = Some(initialContext),
      contextTask = task,
      shouldFireCheckIntervalMillis = shouldFireCheckIntervalMillis,
      concurrency = 1,
      readAndWriteContext = true,
      leaseElectorOverrideForTests = None
    )
}

class GlobalIntervalScheduler private(
  application: KoskiApplication,
  name: String,
  interval: Duration,
  contextTask: Option[JValue] => Option[JValue],
  shouldFireCheckIntervalMillis: Int,
  concurrency: Int, // Lease-slottien määrä. Arvot <= 0 toimivat kuten 1.
  initialContext: Option[JValue],
  readAndWriteContext: Boolean,
  leaseElectorOverrideForTests: Option[WorkerLeaseElector]
) extends QueryMethods with Logging {

  override val db: DB = application.masterDatabase.db

  private val leaseElector: Option[WorkerLeaseElector] = leaseElectorOverrideForTests.orElse(
    Some(new WorkerLeaseElector(
      application.workerLeaseRepository,
      name,
      application.instanceId,
      slots = concurrency,
      leaseDuration = application.config.getDuration("schedule.workerLease.duration"),
      heartbeatInterval = application.config.getDuration("schedule.workerLease.heartbeatInterval")
    ))
  )

  private val taskExecutor = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory(name))
  private val runningTasksOnThisNode = new java.util.concurrent.atomic.AtomicInteger(0)

  @volatile private var suspended = false

  def suspend(): Unit = {
    require(Environment.isUnitTestEnvironment(application.config), "suspend() on sallittu vain testeissä")
    suspended = true
  }
  def unsuspend(): Unit = {
    require(Environment.isUnitTestEnvironment(application.config), "unsuspend() on sallittu vain testeissä")
    suspended = false
  }

  // Insert row if it doesn't exist yet; don't clobber existing rows
  runDbSync(sqlu"""INSERT INTO scheduler (name, nextfiretime, context) VALUES ($name, ${nextFireTime()}, NULL) ON CONFLICT DO NOTHING""")

  private var localNextFireTime: Timestamp = {
    val dbTime = getScheduler.map(_.nextFireTime).getOrElse(nextFireTime())
    if (dbTime.before(now)) {
      val fresh = nextFireTime()
      runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.nextFireTime).update(fresh))
      fresh
    } else {
      dbTime
    }
  }

  // Persist initialContext to DB if the row was just created with NULL context
  if (readAndWriteContext && initialContext.isDefined && getScheduler.flatMap(_.context).isEmpty) {
    runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.context).update(initialContext))
  }

  leaseElector.foreach(_.start(
    onAcquired = slot => logger.info(s"Scheduler $name acquired lease (slot $slot)"),
    onLost = slot => logger.warn(s"Scheduler $name lost lease (slot $slot)")
  ))

  logger.info(s"Starting scheduler $name with interval $interval")

  taskExecutor.scheduleAtFixedRate(() => fireIfTime(), 0, shouldFireCheckIntervalMillis, MILLISECONDS)

  def shutdown(): Unit = {
    taskExecutor.shutdown()
    leaseElector.foreach(_.shutdown())
  }

  def isTaskRunning: Boolean = {
    require(Environment.isUnitTestEnvironment(application.config))
    runningTasksOnThisNode.get() > 0
  }

  def triggerNow(): Unit = {
    logger.info(s"Manually triggering task $name by resetting nextFireTime")
    runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.nextFireTime).update(new Timestamp(0)))
  }

  def hasLease: Boolean = leaseElector.forall(_.hasLease)

  private def fireIfTime(): Unit = {
    try {
      if (shouldFire) {
        try {
          fire()
        } catch {
          case e: Exception =>
            logger.error(e)(s"Scheduled task $name failed: ${e.getMessage}")
        }
      }
    } catch {
      case e: Throwable =>
        logger.error(e)(s"Scheduled task $name encountered fatal error in fireIfTime: ${e.getMessage}")
    }
  }

  private def shouldFire: Boolean = try {
    val hasLease = leaseElector.forall(_.hasLease)
    if (suspended || !hasLease) {
      false
    } else {
      // Read nextFireTime from DB to maintain global cadence across nodes
      val dbNextFireTime = getScheduler.map(_.nextFireTime).getOrElse(localNextFireTime)
      val currentTime = now
      val shouldFire = currentTime.after(dbNextFireTime)
      if (shouldFire) {
        localNextFireTime = nextFireTime(currentTime.toLocalDateTime)
        runningTasksOnThisNode.incrementAndGet()
      }
      shouldFire
    }
  } catch {
    case e: Exception => logger.error(e)(s"Error querying task status $name")
    false
  }

  private def fire(): Unit = try {
    // Write nextFireTime to DB immediately so a new lease holder sees the updated
    // value before the task completes, preventing premature firing.
    runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.nextFireTime).update(localNextFireTime))
    val context: Option[JValue] = if (readAndWriteContext) {
      runDbSync(KoskiTables.Scheduler.filter(_.name === name).result.head).context
    } else {
      None
    }
    logger.debug(s"Firing scheduled task $name ${context.map(c => s"with context ${JsonMethods.compact(c)}").mkString}")
    val newContext: Option[JValue] = contextTask(context)
    if (readAndWriteContext && newContext.isDefined) {
      runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.context).update(newContext))
    }
  } finally {
    runningTasksOnThisNode.decrementAndGet()
  }

  private def nextFireTime(seed: LocalDateTime = LocalDateTime.now): Timestamp =
    Timestamp.valueOf(seed.plus(interval))

  private def now = new Timestamp(currentTimeMillis)

  private def getScheduler: Option[SchedulerRow] = try {
    runDbSync(KoskiTables.Scheduler.filter(s => s.name === name).result.headOption)
  } catch {
    case e: Exception => logger.error(e)(s"Error getting scheduler $name")
    None
  }
}
