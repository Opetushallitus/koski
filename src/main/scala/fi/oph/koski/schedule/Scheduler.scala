package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis
import java.sql.Timestamp
import java.time.{Duration, LocalDateTime}
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

import fi.oph.koski.db.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.executors.NamedThreadFactory
import fi.oph.koski.log.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods
import scala.annotation.tailrec


class Scheduler(
  val db: DB,
  name: String,
  scheduling: Schedule,
  initialContext: Option[JValue],
  task: Option[JValue] => Option[JValue],
  runOnSingleNode: Boolean = true,
  intervalMillis: Int = 10000
) extends QueryMethods with Logging {

  private val taskExecutor = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory(name))
  private val context: Option[JValue] = getScheduler.flatMap(_.context).orElse(initialContext)
  private val firingStrategy = if (runOnSingleNode) new FireOnSingleNode else new FireOnAllNodes

  logger.info(s"Starting ${if (runOnSingleNode) "single" else "multi" } node scheduler $name with $scheduling")
  runDbSync(KoskiTables.Scheduler.insertOrUpdate(
    SchedulerRow(
      name,
      scheduling.nextFireTime(),
      context,
      ScheduledTaskStatus.scheduled,
      None
    )
  ))
  taskExecutor.scheduleAtFixedRate(() => fireIfTime(), 0, intervalMillis, MILLISECONDS)

  def shutdown: Unit = taskExecutor.shutdown()

  private def fireIfTime() = {
    if (shouldFire) {
      try {
        fire
      } catch {
        case e: Exception =>
          logger.error(e)(s"Scheduled task $name failed: ${e.getMessage}")
      }
    } else {
      getScheduler.foreach { scheduler =>
        def runningTimeHours = MILLISECONDS.toHours(currentTimeMillis - scheduler.nextFireTime.getTime)
        if (scheduler.running && runningTimeHours > 24) {
          logger.error(s"Scheduled task $scheduler has been in running state for more than $runningTimeHours hours")
        }
      }
    }
  }

  private def shouldFire = try {
    firingStrategy.shouldFire
  } catch {
    case e: Exception => logger.error(e)(s"Error querying task status $name")
    false
  }

  private def fire = try {
    val context: Option[JValue] = runDbSync(KoskiTables.Scheduler.filter(_.name === name).result.head).context
    logger.debug(s"Firing scheduled task $name ${context.map(c => s"with context ${JsonMethods.compact(c)}").mkString}")
    val newContext: Option[JValue] = task(context)
    if (newContext.isDefined) {
      runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.context).update(newContext))
    }
  } finally {
    firingStrategy.endRun
  }

  private def now = new Timestamp(currentTimeMillis)

  private def getScheduler: Option[SchedulerRow] = try {
    runDbSync(KoskiTables.Scheduler.filter(s => s.name === name).result.headOption)
  } catch {
    case e: Exception => logger.error(e)(s"Error getting scheduler $name")
    None
  }

  trait FiringStrategy {
    def shouldFire: Boolean
    def endRun: Unit
  }

  class FireOnSingleNode extends FiringStrategy {
    override def shouldFire: Boolean = {
      val rowsUpdated = runDbSync(
        KoskiTables.Scheduler
          .filter(s => s.name === name && s.nextFireTime < now && s.status === ScheduledTaskStatus.scheduled && (s.pausedUntil.isEmpty || s.pausedUntil < now))
          .map(s => (s.nextFireTime, s.status))
          .update(scheduling.nextFireTime(), ScheduledTaskStatus.running)
      )
      rowsUpdated > 0
    }

    override def endRun: Unit =
      runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.status).update(ScheduledTaskStatus.scheduled))
  }

  class FireOnAllNodes extends FiringStrategy {
    private var lastFired: Timestamp = new Timestamp(0)

    override def shouldFire: Boolean = {
      val scheduler = getScheduler
      val isPaused = scheduler.exists(_.paused)
      if (isPaused) {
        false
      } else {
        val nextFireTime = scheduling.nextFireTime(lastFired.toLocalDateTime)
        val shouldFire = now.after(nextFireTime)
        if (shouldFire) {
          lastFired = now
        }
        shouldFire
      }
    }

    override def endRun: Unit = ()
  }
}

object Scheduler extends Logging {
  @volatile private var pauseTimeoutMillis = 30000L // 30 seconds
  private val statusCheckIntervalMillis = 100L // Check every 100ms

  // For testing purposes only
  def setPauseTimeoutForTests(timeoutMillis: Long): Unit = {
    pauseTimeoutMillis = timeoutMillis
  }

  def resetPauseTimeout(): Unit = {
    pauseTimeoutMillis = 30000L
  }

  def pauseForDuration(db: DB, name: String, duration: Duration): Boolean = {
    val pausedUntilTime = Timestamp.valueOf(LocalDateTime.now().plus(duration))
    val scheduler = KoskiTables.Scheduler.filter(_.name === name)

    // Set pausedUntil and update nextFireTime to be at least pausedUntil
    val currentRow = QueryMethods.runDbSync(db, scheduler.result.headOption)
    currentRow match {
      case Some(row) =>
        val newNextFireTime = if (row.nextFireTime.before(pausedUntilTime)) pausedUntilTime else row.nextFireTime
        QueryMethods.runDbSync(db, scheduler.map(s => (s.pausedUntil, s.nextFireTime)).update((Some(pausedUntilTime), newNextFireTime)))
      case None =>
        false
    }

    // Wait for running task to complete, but at most until pause ends or absolute timeout
    val absoluteDeadline = System.currentTimeMillis() + pauseTimeoutMillis

    @tailrec
    def waitUntilNotRunning(): Boolean = {
      val now = System.currentTimeMillis()
      if (now >= absoluteDeadline) {
        logger.error(s"Timeout waiting for scheduler $name to stop running during pause")
        throw new RuntimeException(s"Timeout waiting for scheduler $name to stop running. Task has been running for more than ${pauseTimeoutMillis}ms")
      }

      QueryMethods.runDbSync(db, scheduler.result.headOption) match {
        case Some(row) =>
          val pauseEndsAt = row.pausedUntil.map(_.getTime).getOrElse(0L)
          if (now >= pauseEndsAt) {
            // Pause period has ended or was cancelled (resume called), no need to wait anymore
            true
          } else if (row.running) {
            Thread.sleep(statusCheckIntervalMillis)
            waitUntilNotRunning()
          } else {
            true
          }
        case None => false
      }
    }

    waitUntilNotRunning()
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

