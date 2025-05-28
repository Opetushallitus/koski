package fi.oph.koski.massaluovutus.suorituspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits.getKoskiOpiskeluoikeusRow
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, KoskiOpiskeluoikeusRow, KoskiTables, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.{OPHKATSELIJA, OPHPAAKAYTTAJA}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus.SupaOpiskeluoikeus
import fi.oph.koski.massaluovutus.{MassaluovutusQueryParameters, MassaluovutusQueryPriority, QueryResultWriter}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema}

import java.sql.Timestamp
import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt

trait SuorituspalveluQuery extends MassaluovutusQueryParameters with Logging {
  def getOpiskeluoikeusIds(db: DB): Seq[(Int, Timestamp, String)]

  override def priority: Int = MassaluovutusQueryPriority.high

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    val opiskeluoikeudetResult = getOpiskeluoikeusIds(application.masterDatabase.db)
    val resultsByOppija = opiskeluoikeudetResult.groupBy(_._3)

    writer.predictFileCount(resultsByOppija.size / 100)
    resultsByOppija.grouped(100).zipWithIndex.foreach { case (oppijaResult, index) =>
      val supaResponses = oppijaResult.map { case (oppija_oid, opiskeluoikeudet) =>
        val latestTimestamp = opiskeluoikeudet.maxBy(_._2.toInstant)._2
        val db = selectDbByLag(application, latestTimestamp)
        val response = opiskeluoikeudet.flatMap(oo => getOpiskeluoikeus(application, db, oo._1))
        response.foreach { oo =>
          auditLog(oppija_oid, oo.oid)
        }
        SupaResponse(
          oppijaOid = oppija_oid,
          kaikkiOidit = application.henkilöRepository.findByOid(oppija_oid).get.kaikkiOidit,
          aikaleima = LocalDateTime.from(latestTimestamp.toLocalDateTime),
          opiskeluoikeudet = response
        )
      }
      writer.putJson(s"$index", supaResponses)
    }
    Right(())
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasRole(OPHKATSELIJA) || user.hasRole(OPHPAAKAYTTAJA)

  private def getOpiskeluoikeus(application: KoskiApplication, db: DB, id: Int): Option[SupaOpiskeluoikeus] =
    QueryMethods.runDbSync(
      db,
      sql"""
         SELECT *
         FROM opiskeluoikeus
         WHERE id = $id
      """.as[KoskiOpiskeluoikeusRow]
    ).headOption.flatMap(toSupaOpiskeluoikeus(application))

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

  private def toSupaOpiskeluoikeus(application: KoskiApplication)(row: KoskiOpiskeluoikeusRow): Option[SupaOpiskeluoikeus] = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.strictDeserialization)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) =>
        SupaOpiskeluoikeusO(oo)
      case Left(errors) =>
        logger.warn(s"Error deserializing opiskeluoikeus: ${errors}")
        None
    }
  }

  private def auditLog(oppijaOid: String, opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.SUORITUSPALVELU_OPISKELUOIKEUS_HAKU,
          user,
          Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid,
            KoskiAuditLogMessageField.opiskeluoikeusOid -> opiskeluoikeusOid,
          )
        )
      )
}

object SuorituspalveluQuery {
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
