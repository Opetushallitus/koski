package fi.oph.koski.queuedqueries

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import org.json4s.JValue

class QueryScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "kysely"
  val schedulerDb = application.masterDatabase.db
  val concurrency: Int = application.config.getInt("kyselyt.concurrency")
  val kyselyt: QueryService = application.kyselyService
  private var context: QuerySchedulerContext = QuerySchedulerContext(
    workerId = kyselyt.workerId,
    substitute = None,
  )

  sys.addShutdownHook {
    kyselyt.cancelAllTasks("Interrupted: worker shutdown")
  }

  def scheduler: Option[Scheduler] = {
    if (isQueryWorker(context)) {
      val workerId = application.kyselyService.workerId
      logger.info(s"Starting as query worker (id: $workerId)")
      overrideContext()

      Some(new Scheduler(
        schedulerDb,
        schedulerName,
        new IntervalSchedule(application.config.getDuration("kyselyt.checkInterval")),
        Some(context.asJson),
        runNextQuery,
        intervalMillis = 1000
      ))
    } else {
      None
    }
  }

  def getContext: Option[QuerySchedulerContext] =
    Scheduler
      .getContext(schedulerDb, schedulerName)
      .flatMap(parseContext)

  def promote(value: Boolean): Unit = {
    context = context.copy(substitute = Some(value))
    overrideContext()
  }

  private def overrideContext(): Unit = {
    Scheduler.setContext(schedulerDb, schedulerName, Some(context.asJson))
  }

  private def runNextQuery(context: Option[JValue]): Option[JValue] = {
    if (context.flatMap(parseContext).exists(isQueryWorker)) {
      if (kyselyt.numberOfRunningQueries < concurrency) {
        kyselyt.runNext()
      }
    }
    None // QueryScheduler päivitä kontekstia vain käynnistyessään
  }

  private def parseContext(context: JValue): Option[QuerySchedulerContext] =
    application
      .validatingAndResolvingExtractor.extract[QuerySchedulerContext](context, strictDeserialization)
      .toOption

  private def isQueryWorker(context: QuerySchedulerContext) = {
    (context.isSubstitute || QueryUtils.isQueryWorker(application)) && context.workerId == kyselyt.workerId
  }
}

case class QuerySchedulerContext(
  workerId: String,
  substitute: Option[Boolean],
) {
  def isSubstitute: Boolean = substitute.contains(true)
  def asJson: JValue = JsonSerializer.serializeWithRoot(this)
}
