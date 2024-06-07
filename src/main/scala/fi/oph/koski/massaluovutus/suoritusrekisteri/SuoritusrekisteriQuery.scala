package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits.getKoskiOpiskeluoikeusRow
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, KoskiOpiskeluoikeusRow, KoskiTables, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.{OPHKATSELIJA, OPHPAAKAYTTAJA}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.{MassaluovutusQueryParameters, MassaluovutusQueryPriority, QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.sql.Timestamp
import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt

@Title("Suoritusrekisterin kysely")
@Description("Palauttaa Suoritusrekisteriä varten räätälöidyt tiedot annettujen oppijoiden ja koulutusmuodon mukaisista opiskeluoikeuksista.")
case class SuoritusrekisteriQuery(
  @EnumValues(Set("sure"))
  `type`: String = "sure",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  muuttuneetJälkeen: LocalDateTime,
) extends MassaluovutusQueryParameters with Logging {
  override def priority: Int = MassaluovutusQueryPriority.high

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    val opiskeluoikeudet = muuttuneetOpiskeluoikeudet(application.masterDatabase.db)
    writer.predictFileCount(opiskeluoikeudet.size)
    opiskeluoikeudet.grouped(100).foreach { groupedOpiskeluoikeudet =>
      val db = selectDbByLag(application, groupedOpiskeluoikeudet.head._2)
      groupedOpiskeluoikeudet.foreach { case (oid, _) =>
        getOpiskeluoikeus(application, db, oid) match {
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


  private def muuttuneetOpiskeluoikeudet(db: DB): Seq[(Int, Timestamp)] =
    QueryMethods.runDbSync(
      db,
      sql"""
        SELECT id, aikaleima
        FROM opiskeluoikeus
        WHERE aikaleima >= ${Timestamp.valueOf(muuttuneetJälkeen)}
          AND koulutusmuoto = any(${SuoritusrekisteriQuery.opiskeluoikeudenTyypit})
        ORDER BY aikaleima
      """.as[(Int, Timestamp)])

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
