package fi.oph.koski.queuedqueries

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.LoggerWithContext
import fi.oph.koski.schema.Organisaatio.Oid
import software.amazon.awssdk.services.rds.RdsClient

import scala.jdk.CollectionConverters._
import scala.util.{Try, Using}

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

  def defaultOrganisaatio(implicit user: KoskiSpecificSession): Either[HttpStatus, Oid] = {
    val organisaatiot = user.juuriOrganisaatiot
    if (organisaatiot.isEmpty) {
      Left(KoskiErrorCategory.unauthorized("Käyttäjäoikeuksissa ei ole määritelty eksplisiittisesti lukuoikeutta yhdenkään tietyn organisaation tietoihin.")) // Mahdollista esim. pääkäyttäjän tunnuksilla
    } else if (organisaatiot.size > 1) {
      Left(KoskiErrorCategory.unauthorized("Kenttää `organisaatioOid` ei ole annettu, eikä organisaatiota voi yksiselitteisesti päätellä käyttöoikeuksista."))
    } else {
      Right(user.juuriOrganisaatiot.head.oid)
    }
  }

  def QueryResourceManager(logger: LoggerWithContext)(op: Using.Manager => Unit): Either[Oid, Unit] =
    Using.Manager(op)
      .toEither
      .left.map { error =>
        logger.error(error)("Query failed")
        error.toString
      }


}

object QueryFormat {
  val json = "application/json"
  val csv = "text/csv"
  val xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
}
