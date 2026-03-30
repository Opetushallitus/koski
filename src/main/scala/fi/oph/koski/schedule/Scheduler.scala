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


class Scheduler(
  application: KoskiApplication,
  name: String,
  scheduling: Schedule,
  initialContext: Option[JValue],
  task: Option[JValue] => Option[JValue],
  intervalMillis: Int = 10000,
  mode: SchedulerMode = RunOnAllNodes
) extends QueryMethods with Logging {

  override val db: DB = application.masterDatabase.db

  private val leaseElector: Option[WorkerLeaseElector] = mode match {
    case RunOnAllNodes => None
    case m: LeaseControlledWithSharedSchedule => m.leaseElectorOverrideForTests.orElse(Some(createLeaseElector(m.concurrency)))
    case m: LeaseControlledWithIndependentSchedules => m.leaseElectorOverrideForTests.orElse(Some(createLeaseElector(m.concurrency)))
  }

  private def createLeaseElector(slots: Int) = new WorkerLeaseElector(
    application.workerLeaseRepository,
    name,
    application.instanceId,
    slots = slots,
    leaseDuration = application.config.getDuration("schedule.workerLease.duration"),
    heartbeatInterval = application.config.getDuration("schedule.workerLease.heartbeatInterval")
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
  runDbSync(sqlu"""INSERT INTO scheduler (name, nextfiretime, context) VALUES ($name, ${scheduling.nextFireTime()}, NULL) ON CONFLICT DO NOTHING""")

  // Read context after ensuring the row exists
  private val context: Option[JValue] = getScheduler.flatMap(_.context).orElse(initialContext)
  // If nextFireTime is in the past (stale from a previous instance), recompute from now.
  // This prevents fire-on-startup for stale rows.
  // For LeaseControlledWithIndependentSchedules, each node uses its own cadence,
  // so ignore the DB value and fire on first tick.
  private var localNextFireTime: Timestamp = {
    if (mode.independentSchedules) {
      new Timestamp(0) // fire immediately on first tick
    } else {
      val dbTime = getScheduler.map(_.nextFireTime).getOrElse(scheduling.nextFireTime())
      if (dbTime.before(now)) {
        val fresh = scheduling.nextFireTime()
        runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.nextFireTime).update(fresh))
        fresh
      } else {
        dbTime
      }
    }
  }

  // Persist initialContext to DB if the row was just created with NULL context
  if (initialContext.isDefined && getScheduler.flatMap(_.context).isEmpty) {
    runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.context).update(initialContext))
  }

  leaseElector.foreach(_.start(
    onAcquired = slot => logger.info(s"Scheduler $name acquired lease (slot $slot)"),
    onLost = slot => logger.warn(s"Scheduler $name lost lease (slot $slot)")
  ))

  logger.info(s"Starting scheduler $name with $scheduling")

  taskExecutor.scheduleAtFixedRate(() => fireIfTime(), 0, intervalMillis, MILLISECONDS)

  def shutdown: Unit = {
    taskExecutor.shutdown()
    leaseElector.foreach(_.shutdown())
  }

  def isTaskRunning: Boolean = runningTasksOnThisNode.get() > 0
  def hasLease: Boolean = leaseElector.forall(_.hasLease)

  def triggerNow(): Unit = {
    require(Environment.isUnitTestEnvironment(application.config) || leaseElector.forall(_.hasLease),
      "triggerNow() vaatii aktiivisen lease-varauksen")
    taskExecutor.submit(new Runnable {
      def run(): Unit = {
        runningTasksOnThisNode.incrementAndGet()
        try {
          val context: Option[JValue] = runDbSync(KoskiTables.Scheduler.filter(_.name === name).result.head).context
          logger.info(s"Manually triggered task $name")
          val newContext: Option[JValue] = task(context)
          if (newContext.isDefined) {
            runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.context).update(newContext))
          }
        } catch {
          case e: Exception => logger.error(e)(s"Manually triggered task $name failed: ${e.getMessage}")
        } finally {
          runningTasksOnThisNode.decrementAndGet()
        }
      }
    })
  }

  private def fireIfTime() = {
    try {
      if (shouldFire) {
        try {
          fire
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
    val scheduler = getScheduler
    val hasLease = leaseElector.forall(_.hasLease)
    if (suspended || !hasLease) {
      false
    } else {
      // For LeaseControlledWithSharedSchedule, read nextFireTime from DB to maintain
      // global cadence across nodes. Otherwise, use the local copy.
      val nextFireTime = if (leaseElector.isDefined && !mode.independentSchedules) {
        scheduler.map(_.nextFireTime).getOrElse(localNextFireTime)
      } else {
        localNextFireTime
      }
      val currentTime = now
      val shouldFire = currentTime.after(nextFireTime)
      if (shouldFire) {
        localNextFireTime = scheduling.nextFireTime(currentTime.toLocalDateTime)
        runningTasksOnThisNode.incrementAndGet()
      }
      shouldFire
    }
  } catch {
    case e: Exception => logger.error(e)(s"Error querying task status $name")
    false
  }

  private def fire = try {
    // Write nextFireTime to DB immediately so a new lease holder sees the updated
    // value before the task completes, preventing premature firing.
    // Skip for LeaseControlledWithIndependentSchedules — each node tracks its own cadence.
    if (!mode.independentSchedules) {
      runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.nextFireTime).update(localNextFireTime))
    }
    val context: Option[JValue] = runDbSync(KoskiTables.Scheduler.filter(_.name === name).result.head).context
    logger.debug(s"Firing scheduled task $name ${context.map(c => s"with context ${JsonMethods.compact(c)}").mkString}")
    val newContext: Option[JValue] = task(context)
    if (newContext.isDefined) {
      runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.context).update(newContext))
    }
  } finally {
    runningTasksOnThisNode.decrementAndGet()
  }

  private def now = new Timestamp(currentTimeMillis)

  private def getScheduler: Option[SchedulerRow] = try {
    runDbSync(KoskiTables.Scheduler.filter(s => s.name === name).result.headOption)
  } catch {
    case e: Exception => logger.error(e)(s"Error getting scheduler $name")
    None
  }
}

trait Schedule {
  def nextFireTime(seed: LocalDateTime = LocalDateTime.now): Timestamp = Timestamp.valueOf(scheduleNextFireTime(seed))
  def scheduleNextFireTime(seed: LocalDateTime): LocalDateTime
}

class FixedTimeOfDaySchedule(hour: Int, minute: Int) extends Schedule {
  override def scheduleNextFireTime(seed: LocalDateTime): LocalDateTime = seed.plusDays(1).withHour(hour).withMinute(minute)
  override def toString = s"FixedTimeOfDaySchedule(hour=$hour,minute=$minute)"
}

class IntervalSchedule(duration: Duration) extends Schedule {
  override def scheduleNextFireTime(seed: LocalDateTime): LocalDateTime = seed.plus(duration)
  override def toString = s"IntervalSchedule(duration=$duration)"
}

sealed trait SchedulerMode {
  private[schedule] def independentSchedules: Boolean
}

object SchedulerMode extends Logging {
  def leaseControlledWithIndependentSchedules(concurrency: Int): SchedulerMode = {
    if (concurrency >= 2) {
      LeaseControlledWithIndependentSchedules(concurrency)
    } else {
      logger.warn(s"concurrency=$concurrency on liian pieni rinnakkaiselle suoritukselle, käytetään jaettua skedulointia")
      leaseControlledWithSharedSchedule(concurrency)
    }
  }

  def leaseControlledWithSharedSchedule(concurrency: Int): SchedulerMode = {
    if (concurrency >= 1) {
      LeaseControlledWithSharedSchedule(concurrency)
    } else {
      logger.warn(s"concurrency=$concurrency on liian pieni lease-kontrolloidulle suoritukselle, ajetaan kaikilla nodeilla")
      RunOnAllNodes
    }
  }
}

case object RunOnAllNodes extends SchedulerMode {
  private[schedule] val independentSchedules: Boolean = true
}
private[schedule] case class LeaseControlledWithSharedSchedule(
  concurrency: Int,
  private[schedule] val leaseElectorOverrideForTests: Option[WorkerLeaseElector] = None
) extends SchedulerMode {
  private[schedule] val independentSchedules: Boolean = false
}
private[schedule] case class LeaseControlledWithIndependentSchedules(
  concurrency: Int,
  private[schedule] val leaseElectorOverrideForTests: Option[WorkerLeaseElector] = None
) extends SchedulerMode {
  private[schedule] val independentSchedules: Boolean = true
}
