package fi.oph.koski.queuedqueries

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DB
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

import java.time.Duration

class QueryScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "kysely"
  val schedulerDb: DB = application.masterDatabase.db
  val backpressureDuration: Duration = application.config.getDuration("kyselyt.backpressureLimits.duration")
  val concurrency: Int = QueryUtils.concurrency(application.config)
  val kyselyt: QueryService = application.kyselyService

  sys.addShutdownHook {
    kyselyt.cancelAllTasks("Interrupted: worker shutdown")
  }

  def scheduler: Option[Scheduler] = {
    val arn = application.ecsMetadata.taskARN
    val allInstances = application.ecsMetadata.currentlyRunningKoskiInstances
    val workerInstances = QueryUtils.workerInstances(application, concurrency)

    logger.info(s"Check eligibility for query worker: arn = ${arn.getOrElse("n/a")}, allInstances = [${allInstances.map(_.taskArn).mkString(", ")}], workerInstances = [${workerInstances.map(_.taskArn).mkString(", ")}]")

    if (isQueryWorker) {
      val workerId = application.kyselyService.workerId
      logger.info(s"Starting as query worker (id: $workerId)")

      Some(new Scheduler(
        schedulerDb,
        schedulerName,
        new IntervalSchedule(application.config.getDuration("kyselyt.checkInterval")),
        None,
        runNextQuery,
        runOnSingleNode = false,
        intervalMillis = 1000
      ))
    } else {
      None
    }
  }
  
  private def runNextQuery(_context: Option[JValue]): Option[JValue] = {
    if (isQueryWorker) {
      if (kyselyt.hasNext) {
        if (kyselyt.systemIsOverloaded) {
          logger.info(s"System is overloaded. Postponing running the next query for $backpressureDuration")
          Scheduler.pauseForDuration(schedulerDb, schedulerName, backpressureDuration)
        } else {
          kyselyt.runNext()
        }
      }
    }
    None // QueryScheduler päivitä kontekstia vain käynnistyessään
  }

  private def isQueryWorker = QueryUtils.isQueryWorker(application, concurrency)
}
