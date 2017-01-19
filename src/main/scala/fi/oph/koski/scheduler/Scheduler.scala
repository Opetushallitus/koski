package fi.oph.koski.scheduler

import java.lang.System.currentTimeMillis
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.concurrent.{Executors, TimeUnit}

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.Scheduler
import fi.oph.koski.db.{GlobalExecutionContext, KoskiDatabaseMethods, SchedulerRow}
import fi.oph.koski.log.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods

class Scheduler(val db: DB, name: String, scheduling: Schedule, initialContext: Option[JValue], task: Option[JValue] => Option[JValue]) extends GlobalExecutionContext with KoskiDatabaseMethods with Logging {
  private val taskExecutor = Executors.newSingleThreadScheduledExecutor
  runDbSync(Scheduler.insertOrUpdate(SchedulerRow(name, scheduling.nextFireTime, initialContext)))
  taskExecutor.scheduleAtFixedRate(() => fireIfTime(), 10, 10, TimeUnit.SECONDS)

  private def fireIfTime() = {
    val shouldFire = runDbSync(Scheduler.filter(s => s.name === name && s.nextFireTime < now).map(_.nextFireTime).update(scheduling.nextFireTime)) > 0
    if (shouldFire) {
      try {
        fire
      } catch {
        case e: Exception =>
          logger.error(e)(s"Scheduled task $name failed: ${e.getMessage}")
          throw e
      }
    }
  }

  private def fire = {
    val context: Option[JValue] = runDbSync(Scheduler.filter(_.name === name).result.head).context
    logger.info(s"Firing scheduled task $name with context ${context.map(JsonMethods.compact)}")
    val newContext: Option[JValue] = task(context)
    runDbSync(Scheduler.filter(s => s.name === name).map(_.context).update(newContext))
  }

  private def now = new Timestamp(currentTimeMillis)
}

trait Schedule {
  def nextFireTime: Timestamp = Timestamp.valueOf(nextFireTime(LocalDateTime.now))
  def nextFireTime(seed: LocalDateTime): LocalDateTime
}

class IntervalSchedule(seconds: Int) extends Schedule {
  override def nextFireTime(seed: LocalDateTime): LocalDateTime = seed.plusSeconds(seconds)
}

class FixedTimeOfDaySchedule(hour: Int, minute: Int) extends Schedule {
  override def nextFireTime(seed: LocalDateTime): LocalDateTime = seed.plusDays(1).withHour(hour).withMinute(minute)
}

