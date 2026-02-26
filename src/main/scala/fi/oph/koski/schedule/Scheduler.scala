package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis
import java.sql.Timestamp
import java.time.{Duration, LocalDateTime}
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

import com.typesafe.config.Config
import fi.oph.koski.db.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.executors.NamedThreadFactory
import fi.oph.koski.log.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods


class Scheduler(
  val db: DB,
  name: String,
  scheduling: Schedule,
  initialContext: Option[JValue],
  task: Option[JValue] => Option[JValue],
  intervalMillis: Int = 10000,
  config: Config,
  leaseElector: Option[WorkerLeaseElector] = None
) extends QueryMethods with Logging {

  private val taskExecutor = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory(name))
  private val runningTasksOnThisNode = new java.util.concurrent.atomic.AtomicInteger(0)

  // Insert row if it doesn't exist yet; don't clobber existing rows
  runDbSync(sqlu"""INSERT INTO scheduler (name, nextfiretime, context) VALUES ($name, ${scheduling.nextFireTime()}, NULL) ON CONFLICT DO NOTHING""")
  // Always clear pausedUntil on startup (preserves existing behavior)
  runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.pausedUntil).update(None))

  // Read context and lastFired after ensuring the row exists
  private val context: Option[JValue] = getScheduler.flatMap(_.context).orElse(initialContext)
  // Initialize lastFired from DB. If DB value is in the past, use current time
  // so the first fire waits one interval (prevents fire-on-startup for stale rows).
  private var lastFired: Timestamp = {
    val dbTime = getScheduler.map(_.nextFireTime).getOrElse(new Timestamp(0))
    if (dbTime.before(now)) now else dbTime
  }

  // Persist initialContext to DB if the row was just created with NULL context
  if (initialContext.isDefined && getScheduler.flatMap(_.context).isEmpty) {
    runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.context).update(initialContext))
  }

  leaseElector.foreach(_.start())

  logger.info(s"Starting scheduler $name with $scheduling")

  taskExecutor.scheduleAtFixedRate(() => fireIfTime(), 0, intervalMillis, MILLISECONDS)

  def shutdown: Unit = {
    taskExecutor.shutdown()
    leaseElector.foreach(_.shutdown())
  }

  def isTaskRunning: Boolean = runningTasksOnThisNode.get() > 0

  private def fireIfTime() = {
    if (shouldFire) {
      try {
        fire
      } catch {
        case e: Exception =>
          logger.error(e)(s"Scheduled task $name failed: ${e.getMessage}")
      }
    }
  }

  private def shouldFire: Boolean = try {
    val scheduler = getScheduler
    val isPaused = scheduler.exists(_.paused)
    val hasLease = leaseElector.forall(_.hasLease)
    if (isPaused || !hasLease) {
      false
    } else {
      // When using a lease elector, read last fire time from DB to maintain global
      // cadence across nodes. This ensures a new lease holder respects the previous
      // holder's last fire time rather than using stale local state.
      val fireSeed = if (leaseElector.isDefined) {
        scheduler.map(_.nextFireTime).getOrElse(lastFired)
      } else {
        lastFired
      }
      val nextFireTime = scheduling.nextFireTime(fireSeed.toLocalDateTime)
      val shouldFire = now.after(nextFireTime)
      if (shouldFire) {
        lastFired = now
        runningTasksOnThisNode.incrementAndGet()
      }
      shouldFire
    }
  } catch {
    case e: Exception => logger.error(e)(s"Error querying task status $name")
    false
  }

  private def fire = try {
    // Write lastFired to DB immediately so a new lease holder sees the updated
    // nextFireTime before the task completes, preventing premature firing.
    runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.nextFireTime).update(lastFired))
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

object Scheduler extends Logging {
  /** Pauses the named scheduler for the given duration by setting pausedUntil in the database.
   *
   * This is non-blocking: it returns immediately after updating the DB. An already in-flight task
   * will run to completion; only subsequent firings are suppressed. If you need "drain and quiesce"
   * semantics (e.g. before maintenance), wait for `isTaskRunning == false` after pausing. */
  def pauseForDuration(db: DB, name: String, duration: Duration): Boolean = {
    val pausedUntilTime = Timestamp.valueOf(LocalDateTime.now().plus(duration))
    val scheduler = KoskiTables.Scheduler.filter(_.name === name)

    val currentRow = QueryMethods.runDbSync(db, scheduler.result.headOption)
    currentRow match {
      case Some(row) =>
        val newNextFireTime = if (row.nextFireTime.before(pausedUntilTime)) pausedUntilTime else row.nextFireTime
        QueryMethods.runDbSync(db, scheduler.map(s => (s.pausedUntil, s.nextFireTime)).update((Some(pausedUntilTime), newNextFireTime)))
        true
      case None =>
        false
    }
  }

  def resume(db: DB, name: String): Boolean = {
    QueryMethods.runDbSync(db, {
      KoskiTables.Scheduler
        .filter(_.name === name)
        .map(s => (s.pausedUntil, s.nextFireTime))
        .update((None, Timestamp.valueOf(LocalDateTime.now())))
    }) > 0
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
