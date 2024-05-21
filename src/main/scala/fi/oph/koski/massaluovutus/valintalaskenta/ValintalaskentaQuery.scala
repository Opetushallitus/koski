package fi.oph.koski.massaluovutus.valintalaskenta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits.getKoskiOpiskeluoikeusRow
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{KoskiOpiskeluoikeusRow, KoskiTables, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation, Logging}
import fi.oph.koski.massaluovutus.{MassaluovutusQueryParameters, QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema}
import fi.oph.scalaschema.annotation.DefaultValue
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DatabaseConverters, SQLHelpers}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.Rooli.{OPHKATSELIJA, OPHPAAKAYTTAJA}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext

case class ValintalaskentaQuery(
  @EnumValues(Set("valintalaskenta"))
  `type`: String = "valintalaskenta",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  oppijaOids: Seq[String],
  @DefaultValue(Some(ValintalaskentaQuery.defaultKoulutusmuoto))
  koulutusmuoto: Option[String] = None,
  @DefaultValue(Some(ValintalaskentaQuery.defaultSuoritustyypit))
  suoritustyypit: Option[Seq[String]] = None,
) extends MassaluovutusQueryParameters with Logging {
  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    oppijaOids.foreach { oid =>
      val oos = getOpiskeluoikeus(application, oid)
      if (oos.nonEmpty) {
        writer.putJson(oid, ValintalaskentaResult(oid, oos))
        auditLog(oid)
      }
    }
    Right(())
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasRole(OPHKATSELIJA) || user.hasRole(OPHPAAKAYTTAJA)

  override def fillAndValidate(implicit user: KoskiSpecificSession): Either[HttpStatus, MassaluovutusQueryParameters] =
    Right(copy(
      koulutusmuoto = koulutusmuoto.orElse(Some(ValintalaskentaQuery.defaultKoulutusmuoto)),
      suoritustyypit = suoritustyypit.orElse(Some(ValintalaskentaQuery.defaultSuoritustyypit)),
    ))

  private def getOpiskeluoikeus(application: KoskiApplication, oppijaOid: String): Seq[ValintalaskentaOpiskeluoikeus] = {
    QueryMethods.runDbSync(application.masterDatabase.db, sql"""
      WITH kaikki_oidit AS (
        SELECT oid
        FROM henkilo
        WHERE oid = $oppijaOid OR master_oid = $oppijaOid
      )
      SELECT *
      FROM opiskeluoikeus
	    LEFT JOIN kaikki_oidit ON kaikki_oidit.oid = opiskeluoikeus.oppija_oid
      WHERE oppija_oid = kaikki_oidit.oid
         AND koulutusmuoto = ${koulutusmuoto.get}
         AND suoritustyypit && ${suoritustyypit.get}
    """.as[KoskiOpiskeluoikeusRow])
      .flatMap(toResponse(application))
  }

  private def toResponse(application: KoskiApplication)(row: KoskiOpiskeluoikeusRow): Option[ValintalaskentaOpiskeluoikeus] = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.strictDeserialization)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) => Some(ValintalaskentaOpiskeluoikeus(oo))
      case Left(errors) =>
        logger.warn(s"Error deserializing opiskeluoikeus: ${errors}")
        None
    }
  }

  private def auditLog(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit = {
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.VALINTAPALVELU_OPISKELUOIKEUS_HAKU,
          user,
          Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid,
            KoskiAuditLogMessageField.hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(Map(
              "koulutusmuoto" -> koulutusmuoto.toList,
              "suoritustyypit" -> suoritustyypit.toList.flatten,
            ).filter(_._2.nonEmpty))
          )
        )
      )
  }
}

object ValintalaskentaQuery {
  val defaultKoulutusmuoto = "ammatillinenkoulutus"
  val defaultSuoritustyypit = Seq("ammatillinentutkinto", "ammatillinentutkintoosittainen")
}
