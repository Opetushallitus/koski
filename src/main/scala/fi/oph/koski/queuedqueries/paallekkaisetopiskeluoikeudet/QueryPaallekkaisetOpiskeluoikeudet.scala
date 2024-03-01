package fi.oph.koski.queuedqueries.paallekkaisetopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.queuedqueries.QueryUtils.defaultOrganisaatio
import fi.oph.koski.queuedqueries.{QueryParameters, QueryResultWriter}
import fi.oph.koski.raportit.{AikajaksoRaporttiRequest, DataSheet, ExcelWriter, RaportitAccessResolver, RaportitService}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.util.CsvFormatter
import fi.oph.scalaschema.annotation.{EnumValue, OnlyWhen}

import java.time.LocalDate
import scala.util.Try

case class QueryPaallekkaisetOpiskeluoikeudet(
  @EnumValue("paallekkaisetOpiskeluoikeudet")
  `type`: String = "paallekkaisetOpiskeluoikeudet",
  @EnumValue("text/csv")
  @EnumValue("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  format: String = "text/csv",
  organisaatioOid: Option[Organisaatio.Oid] = None,
  language: Option[String] = None,
  alku: LocalDate,
  loppu: LocalDate,
) extends QueryParameters {
  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = Try {
    val raportitService = new RaportitService(application)

    val request = AikajaksoRaporttiRequest(
      oppilaitosOid = organisaatioOid.get,
      downloadToken = None,
      password = "",
      alku = alku,
      loppu = loppu,
      lang = language.get,
    )

    val localizationReader = new LocalizationReader(application.koskiLocalizationRepository, language.get)
    val report = raportitService.paallekkaisetOpiskeluoikeudet(request, localizationReader)

    if (format == "text/csv") {
      val datasheets = report.sheets.collect { case s: DataSheet => s }
      datasheets
        .foreach { sheet =>
          val name = if (datasheets.length > 1) {
            CsvFormatter.snakecasify(sheet.title)
          } else {
            report.filename.replace(".xlsx", "")
          }
          val csv = writer.createCsv[Product](name)
          csv.put(sheet.rows)
          csv.save()
        }
    } else {
      val upload = writer.createStream(report.filename, format)
      ExcelWriter.writeExcel(
        report.workbookSettings,
        report.sheets,
        ExcelWriter.BooleanCellStyleLocalizedValues(localizationReader),
        upload.output,
      )
      upload.save()
    }

    auditLog
  }.toEither.left.map(_.getMessage)

  override def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    organisaatioOids(application).nonEmpty

  override def withDefaults(implicit user: KoskiSpecificSession): Either[HttpStatus, QueryPaallekkaisetOpiskeluoikeudet] =
    for {
      orgOid <- organisaatioOid
        .toRight(defaultOrganisaatio)
        .fold(identity, Right.apply)
      lang <- Right(language.getOrElse(user.lang))
    } yield copy(
      organisaatioOid = Some(orgOid),
      language = Some(lang),
    )

  private def organisaatioOids(application: KoskiApplication): Set[Oid] =
    RaportitAccessResolver(application).kyselyOiditOrganisaatiolle(organisaatioOid.get)

  private def auditLog(implicit user: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(
      OPISKELUOIKEUS_RAPORTTI,
      user,
      Map(hakuEhto -> s"raportti=paallekkaisetopiskeluoikeudet&oppilaitosOid=${organisaatioOid}&alku=${alku}&loppu=${loppu}&lang=${language.get}")))
}
