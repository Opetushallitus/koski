package fi.oph.koski.queuedqueries

import fi.oph.koski.config.{Environment, KoskiApplication}
import software.amazon.awssdk.services.rds.RdsClient
import scala.jdk.CollectionConverters._

object QueryUtils {
  def isQueryWorker(application: KoskiApplication): Boolean = {
    val instanceAz = application.ecsMetadata.availabilityZone
    val databaseAz = getDatabaseAz(application, application.config.getString("kyselyt.readDatabase"))

    (instanceAz, databaseAz) match {
      case (None, None) => true // Lokaali devausinstanssi
      case (Some(a), Some(b)) if a == b => true // Serveri-instanssi, joka samassa az:ssa tietokannan kanssa
      case _ => false
    }
  }

  def getDatabaseAz(application: KoskiApplication, databaseId: String): Option[String] = {
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
