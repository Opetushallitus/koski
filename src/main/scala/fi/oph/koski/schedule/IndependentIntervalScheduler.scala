package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis
import java.sql.Timestamp
import java.time.{Duration, LocalDateTime}
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.executors.NamedThreadFactory
import fi.oph.koski.log.Logging

/** Scheduler, jossa kukin instanssi käyttää omaa paikallista aikatauluaan.
 * Kun concurrency >= 1, lease-kontrollointi rajoittaa aktiivisten instanssien määrää.
 * Kun concurrency = 0, kaikki ajossaolevat instanssit ajavat tehtävää ilman lease-kontrollointia. */
object IndependentIntervalScheduler {
  def apply(
    application: KoskiApplication,
    name: String,
    interval: Duration,
    task: () => Unit,
    shouldFireCheckIntervalMillis: Int,
    concurrency: Int
  ): IndependentIntervalScheduler =
    new IndependentIntervalScheduler(application, name, interval, task, shouldFireCheckIntervalMillis, concurrency, leaseElectorOverrideForTests = None)

  def withLeaseElectorOverrideForTests(
    application: KoskiApplication,
    name: String,
    interval: Duration,
    task: () => Unit,
    shouldFireCheckIntervalMillis: Int,
    concurrency: Int,
    leaseElectorOverrideForTests: WorkerLeaseElector
  ): IndependentIntervalScheduler = {
    require(Environment.isUnitTestEnvironment(application.config), "leaseElectorOverrideForTests sallittu vain testeissä")
    require(concurrency >= 1)

    new IndependentIntervalScheduler(application, name, interval, task, shouldFireCheckIntervalMillis, concurrency, leaseElectorOverrideForTests = Some(leaseElectorOverrideForTests))
  }
}

class IndependentIntervalScheduler private(
  application: KoskiApplication,
  name: String,
  interval: Duration,
  task: () => Unit,
  shouldFireCheckIntervalMillis: Int,
  concurrency: Int,
  leaseElectorOverrideForTests: Option[WorkerLeaseElector]
) extends Logging {

  private val leaseElector: Option[WorkerLeaseElector] =
    if (concurrency >= 1) {
      leaseElectorOverrideForTests.orElse(
        Some(new WorkerLeaseElector(
          application.workerLeaseRepository,
          name,
          application.instanceId,
          slots = concurrency,
          leaseDuration = application.config.getDuration("schedule.workerLease.duration"),
          heartbeatInterval = application.config.getDuration("schedule.workerLease.heartbeatInterval")
        ))
      )
    } else {
      None
    }

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

  // Each node fires on its own cadence — no DB interaction for nextFireTime
  private var localNextFireTime: Timestamp = new Timestamp(0)

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
      // Each node uses its own local cadence
      val currentTime = now
      val shouldFire = currentTime.after(localNextFireTime)
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
    logger.debug(s"Firing scheduled task $name")
    task()
  } finally {
    runningTasksOnThisNode.decrementAndGet()
  }

  private def nextFireTime(seed: LocalDateTime = LocalDateTime.now): Timestamp =
    Timestamp.valueOf(seed.plus(interval))

  private def now = new Timestamp(currentTimeMillis)
}
