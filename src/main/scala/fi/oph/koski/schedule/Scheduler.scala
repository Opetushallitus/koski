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
import fi.oph.koski.util.SystemInfo
import org.json4s._
import org.json4s.jackson.JsonMethods

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
      ScheduledTaskStatus.scheduled
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
          // SystemInfo.logInfo
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
    runDbSync(KoskiTables.Scheduler.filter(_.name === name).map(_.context).update(newContext))
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
          .filter(s => s.name === name && s.nextFireTime < now && s.status === ScheduledTaskStatus.scheduled)
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
      val nextFireTime = scheduling.nextFireTime(lastFired.toLocalDateTime)
      val shouldFire = now.after(nextFireTime)
      if (shouldFire) {
        lastFired = now
      }
      shouldFire
    }

    override def endRun: Unit = ()
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

