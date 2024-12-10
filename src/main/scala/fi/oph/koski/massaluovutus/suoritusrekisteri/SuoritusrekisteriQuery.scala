package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits.getKoskiOpiskeluoikeusRow
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, KoskiOpiskeluoikeusRow, KoskiTables, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.{OPHKATSELIJA, OPHPAAKAYTTAJA}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.{MassaluovutusQueryParameters, MassaluovutusQueryPriority, QueryResultWriter}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema}

import java.sql.Timestamp
import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt

trait SuoritusrekisteriQuery extends MassaluovutusQueryParameters with Logging {
  def getOpiskeluoikeusIds(db: DB): Seq[(Int, Timestamp, String)]

  override def priority: Int = MassaluovutusQueryPriority.high

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    val opiskeluoikeudetResult = getOpiskeluoikeusIds(application.masterDatabase.db)
    val oppijaOidit = opiskeluoikeudetResult.groupBy(_._3)

    writer.predictFileCount(oppijaOidit.size)
    oppijaOidit.grouped(100).foreach { groupedResult =>
      val db = selectDbByLag(application, groupedResult.head._2.head._2)
      groupedResult.foreach { case (oppija_oid, opiskeluoikeudet) =>
        opiskeluoikeudet.map(oo => getOpiskeluoikeus(application, db, oo._1)).foreach {
          case Some(response) =>
            val ooTyyppi = response.opiskeluoikeus.tyyppi.koodiarvo
            val ptsTyyppi = response.opiskeluoikeus.suoritukset.map(_.tyyppi.koodiarvo).mkString("-")
            writer.putJson(s"$ooTyyppi-$ptsTyyppi-${response.opiskeluoikeus.oid}", response)
            auditLog(response.oppijaOid, response.opiskeluoikeus.oid)
          case None =>
            writer.skipFile()
        }
      }
    }
    Right(())
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasRole(OPHKATSELIJA) || user.hasRole(OPHPAAKAYTTAJA)

  private def getOpiskeluoikeus(application: KoskiApplication, db: DB, id: Int): Option[SureResponse] =
    QueryMethods.runDbSync(
      db,
      sql"""
         SELECT *
         FROM opiskeluoikeus
         WHERE id = $id
      """.as[KoskiOpiskeluoikeusRow]
    ).headOption.flatMap(toResponse(application))

  private def selectDbByLag(application: KoskiApplication, opiskeluoikeusAikaleima: Timestamp): DB = {
    val safetyLimit = 15.seconds
    val replicaLag = application.replicaDatabase.replayLag
    val totalLagSeconds = safetyLimit.toSeconds + replicaLag.toSeconds
    if (opiskeluoikeusAikaleima.toLocalDateTime.plusSeconds(totalLagSeconds).isAfter(LocalDateTime.now())) {
      logger.warn(s"Using master database for query due to replica lag of $replicaLag")
      application.masterDatabase.db
    } else {
      application.replicaDatabase.db
    }
  }

  private def toResponse(application: KoskiApplication)(row: KoskiOpiskeluoikeusRow): Option[SureResponse] = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.strictDeserialization)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) =>
        SureOpiskeluoikeus(oo).map(SureResponse(row.oppijaOid, row.aikaleima.toLocalDateTime, _))
      case Left(errors) =>
        logger.warn(s"Error deserializing opiskeluoikeus: ${errors}")
        None
    }
  }

  private def auditLog(oppijaOid: String, opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.SUORITUSREKISTERI_OPISKELUOIKEUS_HAKU,
          user,
          Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid,
            KoskiAuditLogMessageField.opiskeluoikeusOid -> opiskeluoikeusOid,
          )
        )
      )
}

object SuoritusrekisteriQuery {
  def opiskeluoikeudenTyypit: List[String] = List(
    "perusopetus",
    "aikuistenperusopetus",
    "ammatillinenkoulutus",
    "tuva",
    "vapaansivistystyonkoulutus",
    "diatutkinto",
    "ebtutkinto",
    "ibtutkinto",
    "internationalschool",
  )
}
