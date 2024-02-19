package fi.oph.koski.kyselyt

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue
import software.amazon.awssdk.services.rds.RdsClient

import scala.jdk.CollectionConverters._

class KyselyScheduler(application: KoskiApplication) extends Logging {
  val concurrency: Int = application.config.getInt("kyselyt.concurrency")
  val kyselyt = application.kyselyService

  sys.addShutdownHook {
    kyselyt.cancelAllTasks("Interrupted: worker shutdown")
  }

  def scheduler: Option[Scheduler] = {
    if (isQueryWorker) {
      Some(new Scheduler(
        application.masterDatabase.db,
        "kysely",
        new IntervalSchedule(application.config.getDuration("kyselyt.checkInterval")),
        None,
        runNextQuery,
        intervalMillis = 1000
      ))
    } else {
      None
    }
  }

  lazy val isQueryWorker: Boolean = {
    val instanceAz = application.ecsMetadata.availabilityZone
    logger.info(s"Instance availability zone: $instanceAz")
    val databaseAz = getDatabaseAz("koski-database-replica")
    logger.info(s"Database availability zone: $databaseAz")

    (instanceAz, databaseAz) match {
      case (None, None)                 => true // Lokaali devausinstanssi
      case (Some(a), Some(b)) if a == b => true // Serveri-instanssi, joka samassa az:ssa tietokannan kanssa
      case _                            => false
    }
  }

  private def runNextQuery(_ignore: Option[JValue]): Option[JValue] = {
    if (kyselyt.numberOfRunningQueries < concurrency) {
      kyselyt.runNext()
    }
    None
  }

  private def getDatabaseAz(databaseId: String): Option[String] = {
    if (Environment.isMockEnvironment(application.config)) {
      None
    } else {
      RdsClient
        .create()
        .describeDBInstances()
        .dbInstances()
        .asScala
        .find(_.dbInstanceIdentifier() == databaseId)
        .map(_.availabilityZone())
    }
  }
}
