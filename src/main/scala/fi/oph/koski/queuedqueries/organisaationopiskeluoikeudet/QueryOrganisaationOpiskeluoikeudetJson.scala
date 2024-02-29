package fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DatabaseConverters, KoskiTables, QueryMethods, SQLHelpers}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.queuedqueries.{QueryParameters, QueryResultWriter}
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_HAKU
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.schema.{KoskiSchema, Opiskeluoikeus, Oppija, Organisaatio}
import fi.oph.scalaschema.annotation.EnumValue
import org.json4s.JValue

import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class QueryOrganisaationOpiskeluoikeudetJson(
  @EnumValue("organisaationOpiskeluoikeudet")
  `type`: String = "organisaationOpiskeluoikeudet",
  @EnumValue("application/json")
  format: String = "application/json",
  organisaatioOid: Option[Organisaatio.Oid],
  alkamispaiva: LocalDate,
  tila: Option[String], // TODO: Lisää sallitut arvot
  koulutusmuoto: Option[String], // TODO: Lisää sallitut arvot
  suoritustyyppi: Option[String], // TODO: Lisää sallitut arvot
) extends QueryParameters with DatabaseConverters with Logging {

  def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    try {
      val oppilaitosOids = application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid.get)
      fetchData(
        application = application,
        writer = writer,
        oppilaitosOids = oppilaitosOids,
      )
      auditLog()
      Right(())
    } catch {
      case t: Throwable =>
        logger.error(t)("Kysely epäonnistui")
        Left(t.getMessage)
    }
  }

  private def fetchData(
    application: KoskiApplication,
    writer: QueryResultWriter,
    oppilaitosOids: List[Organisaatio.Oid],
  )(implicit user: KoskiSpecificSession): Unit = {
    val db = application.replicaDatabase.db

    val filters = SQLHelpers.concatMany(
      Some(sql"WHERE NOT poistettu AND NOT mitatoity AND oppilaitos_oid = ANY($oppilaitosOids) AND alkamispaiva >= $alkamispaiva "),
      tila.map(t => sql" AND tila = $t "),
      koulutusmuoto.map(t => sql"AND koulutusmuoto = $t "),
      suoritustyyppi.map(t => sql"AND $t IN suoritustyypit "),
    )

    val oppijaOids = QueryMethods.runDbSync(
      db,
      SQLHelpers.concat(sql"SELECT DISTINCT oppija_oid FROM opiskeluoikeus ", filters).as[(String)]
    )

    oppijaOids.foreach { oppijaOid =>
      application.henkilöRepository.findByOid(oppijaOid, findMasterIfSlaveOid = true).map { henkilö =>
        val henkilöFilter = SQLHelpers.concat(filters, sql"AND oppija_oid = ANY(${henkilö.kaikkiOidit})")
        val opiskeluoikeudet = QueryMethods.runDbSync(
          db,
          SQLHelpers.concat(sql"SELECT data, oid, versionumero, aikaleima FROM opiskeluoikeus ", henkilöFilter).as[(JValue, String, Int, Timestamp)]
        )
        val data = Oppija(
          henkilö = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö),
          opiskeluoikeudet = opiskeluoikeudet.map { row =>
            val (data, oid, versionumero, aikaleima) = row
            val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(data, oid, versionumero, aikaleima)
            application.validatingAndResolvingExtractor.extract[Opiskeluoikeus](KoskiSchema.strictDeserialization)(json) match {
              case Right(oo) => oo
              case Left(errors) => throw new Exception(s"Error deserializing opiskeluoikeus ${oid}: ${errors}")
            }
          },
        )
        writer.putJson(henkilö.oid, data)
      }
    }
  }

  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasGlobalReadAccess || (
      organisaatioOid.exists(user.organisationOids(AccessType.read).contains)
        && koulutusmuoto.forall(user.allowedOpiskeluoikeusTyypit.contains)
      )

  override def withDefaults(implicit user: KoskiSpecificSession): Either[HttpStatus, QueryOrganisaationOpiskeluoikeudetJson] =
    if (organisaatioOid.isEmpty) {
      defaultOrganisaatio.map(oid => copy(organisaatioOid = Some(oid)))
    } else {
      Right(this)
    }

  private def defaultOrganisaatio(implicit user: KoskiSpecificSession) = {
    val organisaatiot = user.juuriOrganisaatiot
    if (organisaatiot.isEmpty) {
      Left(KoskiErrorCategory.unauthorized("Käyttäjäoikeuksissa ei ole määritelty eksplisiittisesti lukuoikeutta yhdenkään tietyn organisaation tietoihin.")) // Mahdollista esim. pääkäyttäjän tunnuksilla
    } else if (organisaatiot.size > 1) {
      Left(KoskiErrorCategory.unauthorized("Kenttää `organisaatioOid` ei ole annettu, eikä organisaatiota voi yksiselitteisesti päätellä käyttöoikeuksista."))
    } else {
      Right(user.juuriOrganisaatiot.head.oid)
    }
  }

  private def auditLog()(implicit user: KoskiSpecificSession): Unit = {
    AuditLog.log(KoskiAuditLogMessage(
      OPISKELUOIKEUS_HAKU,
      user,
      Map(hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(Map(
        "organisaatio" -> List(organisaatioOid.get),
        "opiskeluoikeusAlkanutAikaisintaan" -> List(alkamispaiva.format(DateTimeFormatter.ISO_DATE)),
        "opiskeluoikeudenTila" -> tila.toList,
        "suorituksenTyyppi" -> suoritustyyppi.toList,
        "koulutusmuoto" -> koulutusmuoto.toList,
      ).filter(_._2.nonEmpty))),
    ))
  }
}

