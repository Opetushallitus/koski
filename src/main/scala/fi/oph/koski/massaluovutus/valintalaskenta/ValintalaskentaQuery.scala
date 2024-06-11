package fi.oph.koski.massaluovutus.valintalaskenta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits.getKoskiOpiskeluoikeusRow
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, KoskiOpiskeluoikeusRow, KoskiTables, QueryMethods}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.{OPHKATSELIJA, OPHPAAKAYTTAJA}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.{MassaluovutusQueryParameters, MassaluovutusQueryPriority, QueryFormat, QueryResultWriter}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema}
import fi.oph.koski.util.Collections.asNonEmpty
import fi.oph.scalaschema.annotation.{DefaultValue, Description, Title}
import slick.jdbc.GetResult

import java.sql.Timestamp
import java.time.LocalDate

@Title("Valintalaskennan kysely")
@Description("Palauttaa valintalaskentaa varten räätälöidyt tiedot annettujen oppijoiden ja koulutusmuodon mukaisista opiskeluoikeuksista.")
@Description("Vastauksen skeema on saatavana <a href=\"/koski/json-schema-viewer/?schema=valintalaskenta-result.json\">täältä.</a>")
case class ValintalaskentaQuery(
  @EnumValues(Set("valintalaskenta"))
  `type`: String = "valintalaskenta",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Leikkuripäivä, johon mennessä toimitetut tiedot KOSKI-palveluun haetaan.")
  rajapäivä: LocalDate,
  @Description("Lista oppija-oideista, joiden opiskeluoikeudet haetaan. Rajapinta hyväksyy sekä master- että slave-oidit.")
  oppijaOids: Seq[String],
  @Description(s"Haettavat koulutusmuoto. Oletuksena ammatillinenkoulutus")
  @DefaultValue(Some(ValintalaskentaQuery.defaultKoulutusmuoto))
  koulutusmuoto: Option[String] = None,
  @Description(s"Haettavat suoritustyypit. Oletuksena [ammatillinentutkinto, ammatillinentutkintoosittainen]]")
  @DefaultValue(Some(ValintalaskentaQuery.defaultSuoritustyypit))
  suoritustyypit: Option[Seq[String]] = None,
) extends MassaluovutusQueryParameters with Logging {

  private val aikaraja: Timestamp = Timestamp.valueOf(rajapäivä.plusDays(1).atStartOfDay())
  override def priority: Int = MassaluovutusQueryPriority.highest

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    writer.predictFileCount(oppijaOids.size)
    oppijaOids.foreach { oid =>
      val results = getOpiskeluoikeudet(application, oid)
      if (results.nonEmpty) {
        val opiskeluoikeudet = asNonEmpty(results.collect { case Right(oo) => oo })
        val virheet = asNonEmpty(results.collect { case Left(err) => err })
        writer.putJson(oid, ValintalaskentaResult(oid, opiskeluoikeudet, virheet))
        if (opiskeluoikeudet.nonEmpty) {
          auditLog(oid)
        }
      } else {
        writer.skipFile()
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
      oppijaOids = oppijaOids.distinct,
    ))

  private def getOpiskeluoikeudet(application: KoskiApplication, oppijaOid: String)(implicit user: KoskiSpecificSession): List[Either[ValintalaskentaError, ValintalaskentaOpiskeluoikeus]] =
    try {
      getOpiskeluoikeuksienVersiot(application.replicaDatabase.db, oppijaOid).flatMap { oo =>
        if (oo.rajapäivänVersio == oo.uusinVersio) {
          Some(getUusinOpiskeluoikeus(application, oo.id))
        } else if (oo.rajapäivänVersio > 0) {
          Some(getOpiskeluoikeusHistoriasta(application, oo.oid, oo.rajapäivänVersio))
        } else {
          None
        }
      }.toList
    } catch {
      case t: Throwable => List(Left(ValintalaskentaError(t)))
    }

  private def getUusinOpiskeluoikeus(application: KoskiApplication, id: Int): Either[ValintalaskentaError, ValintalaskentaOpiskeluoikeus] =
    QueryMethods.runDbSync(application.replicaDatabase.db, sql"""
          SELECT *
          FROM opiskeluoikeus
          WHERE ID=$id
        """.as[KoskiOpiskeluoikeusRow])
      .headOption
      .flatMap(toResponse(application))
      .toRight(ValintalaskentaError.internal(s"getUusinOpiskeluoikeus ei löytänyt opiskeluoikeutta $id"))

  private def getOpiskeluoikeusHistoriasta(application: KoskiApplication, oid: String, versio: Int)(implicit user: KoskiSpecificSession): Either[ValintalaskentaError, ValintalaskentaOpiskeluoikeus] =
      application.historyRepository
        .findVersion(oid, versio)
        .map(ValintalaskentaOpiskeluoikeus.apply)
        .left.map(ValintalaskentaError.apply)

  private def getOpiskeluoikeuksienVersiot(db: DB, oppijaOid: String): Seq[OpiskeluoikeudenVersiotieto] =
    QueryMethods.runDbSync(db, sql"""
      WITH kaikki_oidit AS (
        SELECT oid
        FROM henkilo
        WHERE oid = $oppijaOid OR master_oid = $oppijaOid
      )
      SELECT
        opiskeluoikeus.id,
        opiskeluoikeus.oid,
        opiskeluoikeus.versionumero AS uusin_versio,
        max(opiskeluoikeushistoria.versionumero) AS rajapaivan_versio
      FROM opiskeluoikeus
      LEFT JOIN kaikki_oidit ON kaikki_oidit.oid = opiskeluoikeus.oppija_oid
      LEFT JOIN opiskeluoikeushistoria ON
        opiskeluoikeushistoria.opiskeluoikeus_id = opiskeluoikeus.id
        AND opiskeluoikeushistoria.aikaleima < $aikaraja
      WHERE oppija_oid = kaikki_oidit.oid
        AND NOT mitatoity
        AND NOT poistettu
        AND koulutusmuoto = ${koulutusmuoto.get}
        AND suoritustyypit && ${suoritustyypit.get}
        AND alkamispaiva < $aikaraja
      GROUP BY opiskeluoikeus.id, opiskeluoikeus.oid, opiskeluoikeus.versionumero
    """.as[OpiskeluoikeudenVersiotieto])

  private def toResponse(application: KoskiApplication)(row: KoskiOpiskeluoikeusRow): Option[ValintalaskentaOpiskeluoikeus] = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.strictDeserialization)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) => Some(ValintalaskentaOpiskeluoikeus(oo))
      case Left(errors) =>
        logger.warn(s"Error deserializing opiskeluoikeus: ${errors}")
        None
    }
  }

  private def auditLog(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit =
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

  implicit val getOpiskeluoikeudenVersiot: GetResult[OpiskeluoikeudenVersiotieto] = GetResult {
    r => OpiskeluoikeudenVersiotieto(
      id = r.rs.getInt("id"),
      oid = r.rs.getString("oid"),
      uusinVersio = r.rs.getInt("uusin_versio"),
      rajapäivänVersio = r.rs.getInt("rajapaivan_versio"),
    )
  }
}

case class OpiskeluoikeudenVersiotieto(
  id: Int,
  oid: String,
  uusinVersio: Int,
  rajapäivänVersio: Int,
)

object ValintalaskentaQuery {
  val defaultKoulutusmuoto = "ammatillinenkoulutus"
  val defaultSuoritustyypit = Seq("ammatillinentutkinto", "ammatillinentutkintoosittainen")
}

object ValintalaskentaQueryDocumentation {
  def example = ValintalaskentaQuery(
    oppijaOids = oppijaOids,
    rajapäivä = LocalDate.now(),
  )

  def outputFiles: List[String] =
    oppijaOids.map(oid => s"$oid.json")

  def oppijaOids: List[String] =
    List("1.2.246.562.24.37998958910", "1.2.246.562.24.62432463004")
}
