package fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits._
import fi.oph.koski.db._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_HAKU
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.queuedqueries.QueryUtils.defaultOrganisaatio
import fi.oph.koski.queuedqueries.{QueryParameters, QueryResultWriter}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.ChainingSyntax.chainingOps
import slick.jdbc.SQLActionBuilder

import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait QueryOrganisaationOpiskeluoikeudet extends QueryParameters with DatabaseConverters with Logging {
  def organisaatioOid: Option[String]
  def alkamispaiva: LocalDate
  def tila: Option[String] // TODO: Lisää sallitut arvot
  def koulutusmuoto: Option[String] // TODO: Lisää sallitut arvot
  def suoritustyyppi: Option[String] // TODO: Lisää sallitut arvot

  def fetchData(application: KoskiApplication, writer: QueryResultWriter, oppilaitosOids: List[Organisaatio.Oid]): Either[String, Unit]

  def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    val oppilaitosOids = application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid.get)
    fetchData(
      application = application,
      writer = writer,
      oppilaitosOids = oppilaitosOids,
    ).tap(_ => auditLog)
  }

  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasGlobalReadAccess || (
      organisaatioOid.exists(user.organisationOids(AccessType.read).contains)
        && koulutusmuoto.forall(user.allowedOpiskeluoikeusTyypit.contains)
      )

  override def withDefaults(implicit user: KoskiSpecificSession): Either[HttpStatus, QueryOrganisaationOpiskeluoikeudet] = {
    import mojave._
    if (organisaatioOid.isEmpty) {
      defaultOrganisaatio.map { oid =>
        shapeless
          .lens[QueryOrganisaationOpiskeluoikeudet]
          .field[Option[String]]("organisaatioOid")
          .set(this)(Some(oid))
      }
    } else {
      Right(this)
    }
  }

  protected def auditLog(implicit user: KoskiSpecificSession): Unit = {
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

  protected def getDb(application: KoskiApplication): DB = application.replicaDatabase.db

  protected def defaultBaseFilter(oppilaitosOids: List[Organisaatio.Oid]) = SQLHelpers.concatMany(
    Some(sql"WHERE NOT poistettu AND NOT mitatoity AND oppilaitos_oid = ANY($oppilaitosOids) AND alkamispaiva >= $alkamispaiva "),
    tila.map(t => sql" AND tila = $t "),
    koulutusmuoto.map(t => sql"AND koulutusmuoto = $t "),
    suoritustyyppi.map(t => sql"AND $t IN suoritustyypit "),
  )

  protected def getOppijaOids(db: DB, filters: SQLActionBuilder): Seq[String] = QueryMethods.runDbSync(
    db,
    SQLHelpers.concat(sql"SELECT DISTINCT oppija_oid FROM opiskeluoikeus ", filters).as[(String)]
  )

  protected def forEachOpiskeluoikeus(
    application: KoskiApplication,
    filters: SQLActionBuilder,
    oppijaOids: Seq[String])(f: (LaajatOppijaHenkilöTiedot, Seq[KoskiOpiskeluoikeusRow]) => Unit,
  ): Unit =
    oppijaOids.foreach {
      application.henkilöRepository.findByOid(_, findMasterIfSlaveOid = true).map { henkilö =>
        val henkilöFilter = SQLHelpers.concat(filters, sql"AND oppija_oid = ANY(${henkilö.kaikkiOidit})")
        val opiskeluoikeudet = QueryMethods.runDbSync(
          getDb(application),
          SQLHelpers.concat(sql"SELECT * FROM opiskeluoikeus ", henkilöFilter).as[KoskiOpiskeluoikeusRow]
        )
        f(henkilö, opiskeluoikeudet)
      }
    }
}
