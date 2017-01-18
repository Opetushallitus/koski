package fi.oph.koski.scheduler

import java.lang.System.currentTimeMillis
import java.sql.Timestamp
import java.util.concurrent.{Executors, TimeUnit}

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.Scheduler
import fi.oph.koski.db.{GlobalExecutionContext, KoskiDatabaseMethods, SchedulerRow}
import org.joda.time.DateTime
import org.joda.time.DateTime.now

class Scheduler(val db: DB, name: String, scheduling: Scheduling, task: () => Unit) extends GlobalExecutionContext with KoskiDatabaseMethods {
  private val taskExecutor = Executors.newSingleThreadScheduledExecutor
  runDbSync(Scheduler.insertOrUpdate(SchedulerRow(name, scheduling.nextFireTime)))
  taskExecutor.scheduleAtFixedRate(() => fireIfTime(), 10, 10, TimeUnit.SECONDS)

  private def fireIfTime() = if (shouldFire) task()
  private def shouldFire: Boolean =
    runDbSync(Scheduler.filter(s => s.name === name && s.nextFireTime < now).update(SchedulerRow(name, scheduling.nextFireTime))) > 0

  private def now = new Timestamp(currentTimeMillis)
}

trait Scheduling {
  def nextFireTime: Timestamp = new Timestamp(nextFireTime(now).getMillis)
  def nextFireTime(seed: DateTime): DateTime
}

class IntervalScheduling(secs: Int) extends Scheduling {
  override def nextFireTime(seed: DateTime): DateTime = seed.plusSeconds(secs)
}

class FixedTimeOfDayScheduling(hour: Int, minute: Int) extends Scheduling {
  override def nextFireTime(seed: DateTime): DateTime = seed.plusDays(1).withHourOfDay(hour).withMinuteOfHour(minute)
}

object SchedulerApp extends App {
  val application = fi.oph.koski.config.KoskiApplication.apply
  for (x <- 1 to 100) {
    //new Scheduler(application.database.db, "henkilötiedot-update", new IntervalScheduling(10), () => println(s"Server $x executing"))
    new Scheduler(application.database.db, "henkilötiedot-update", new FixedTimeOfDayScheduling(20, 48), () => println(s"Server $x executing"))
    Thread.sleep(1)
  }
}

