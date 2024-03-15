package fi.oph.koski.queuedqueries

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.LoggerWithContext
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.util.TryWithLogging
import software.amazon.awssdk.services.rds.RdsClient

import java.security.SecureRandom
import scala.jdk.CollectionConverters._
import scala.util.Using

object QueryUtils {
  def readDatabaseId(config: Config): String = config.getString("kyselyt.readDatabase")

  def isQueryWorker(application: KoskiApplication): Boolean = {
    val instanceAz = application.ecsMetadata.availabilityZone
    val databaseAz = getDatabaseAz(application, readDatabaseId(application.config))

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
      Left(KoskiErrorCategory.forbidden.organisaatio())
    } else if (organisaatiot.size > 1) {
      Left(KoskiErrorCategory.badRequest.kyselyt.eiYksiselitteinenOrganisaatio())
    } else {
      Right(user.juuriOrganisaatiot.head.oid)
    }
  }

  def QueryResourceManager(logger: LoggerWithContext)(op: Using.Manager => Unit): Either[String, Unit] =
    TryWithLogging.andResources(logger, op).left.map(_.getMessage)

  def generatePassword(length: Int): String = {
    val alphanumericChars = ('0' to '9') ++ ('A' to 'Z') ++ ('a' to 'z')
    val random = new SecureRandom()
    Iterator
      .continually(alphanumericChars(random.nextInt(alphanumericChars.length)))
      .take(length)
      .mkString
  }
}

object QueryFormat {
  val json = "application/json"
  val csv = "text/csv"
  val xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
}
