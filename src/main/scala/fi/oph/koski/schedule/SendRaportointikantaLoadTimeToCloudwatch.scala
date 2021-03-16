package fi.oph.koski.schedule

import java.time.Duration
import fi.oph.koski.cloudwatch.CloudWatchMetrics
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.Timing
import org.json4s.JValue

class SendRaportointikantaLoadTimeToCloudwatch(app: KoskiApplication) extends Timing {
  def scheduler: Option[Scheduler] = Some(new Scheduler(
    app.masterDatabase.db,
    "send-raportointikanta-load-time-to-cloudwatch",
    new IntervalSchedule(Duration.ofMinutes(5)),
    None,
    tryRun
  ))

  private def tryRun(unused: Option[JValue]) = timed("sendLoadTime") {
    try {
      run()
    } catch {
      case e: Exception =>
        logger.error(e)("Failed to send raportointikanta load time to Cloudwatch")
    }
    None
  }

  private def run(): Unit = {
    CloudWatchMetrics.putRaportointikantaLoadtime(
      raportointiDatabase.status.startedTime.get,
      raportointiDatabase.status.completionTime.get
    )
  }
  private lazy val raportointiDatabase = app.raportointiDatabase
}
