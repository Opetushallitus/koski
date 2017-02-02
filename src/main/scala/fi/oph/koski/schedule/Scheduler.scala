package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis
import java.sql.Timestamp
import java.time.{Duration, LocalDateTime}
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{GlobalExecutionContext, KoskiDatabaseMethods, SchedulerRow, Tables}
import fi.oph.koski.log.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods

class Scheduler(val db: DB, name: String, scheduling: Schedule, initialContext: Option[JValue], task: Option[JValue] => Option[JValue], intervalMillis: Int = 10000)
  extends GlobalExecutionContext with KoskiDatabaseMethods with Logging {
  private val taskExecutor = Executors.newSingleThreadScheduledExecutor
  private val context: Option[JValue] = getScheduler.flatMap(_.context).orElse(initialContext)

  runDbSync(Tables.Scheduler.insertOrUpdate(SchedulerRow(name, scheduling.nextFireTime, context, 0)))
  taskExecutor.scheduleAtFixedRate(() => fireIfTime(), intervalMillis, intervalMillis, MILLISECONDS)

  private def fireIfTime() = {
    val shouldFire = runDbSync(Tables.Scheduler.filter(s => s.name === name && s.nextFireTime < now && s.status === 0).map(s => (s.nextFireTime, s.status)).update(scheduling.nextFireTime, 1)) > 0
    if (shouldFire) {
      try {
        fire
      } catch {
        case e: Exception =>
          logger.error(e)(s"Scheduled task $name failed: ${e.getMessage}")
          throw e
      }
    } else {
      val scheduler = getScheduler.get
      def runningTimeHours = MILLISECONDS.toHours(currentTimeMillis - scheduler.nextFireTime.getTime)
      if (scheduler.running && runningTimeHours > 24) {
        logger.error(s"Scheduled task $scheduler has been in running state for more than $runningTimeHours hours")
      }
    }
  }

  private def fire = try {
    val context: Option[JValue] = runDbSync(Tables.Scheduler.filter(_.name === name).result.head).context
    logger.debug(s"Firing scheduled task $name ${context.map(c => s"with context ${JsonMethods.compact(c)}").mkString}")
    val newContext: Option[JValue] = task(context)
    runDbSync(Tables.Scheduler.filter(_.name === name).map(_.context).update(newContext))
  } finally {
    endRun
  }

  private def endRun = runDbSync(Tables.Scheduler.filter(_.name === name).map(_.status).update(0))
  private def now = new Timestamp(currentTimeMillis)
  private def getScheduler: Option[SchedulerRow] =
    runDbSync(Tables.Scheduler.filter(s => s.name === name).result.headOption)
}

trait Schedule {
  def nextFireTime: Timestamp = Timestamp.valueOf(scheduleNextFireTime(LocalDateTime.now))
  def scheduleNextFireTime(seed: LocalDateTime): LocalDateTime
}

class FixedTimeOfDaySchedule(hour: Int, minute: Int) extends Schedule {
  override def scheduleNextFireTime(seed: LocalDateTime): LocalDateTime = seed.plusDays(1).withHour(hour).withMinute(minute)
}

class IntervalSchedule(duration: Duration) extends Schedule {
  override def scheduleNextFireTime(seed: LocalDateTime): LocalDateTime = seed.plus(duration)
}

