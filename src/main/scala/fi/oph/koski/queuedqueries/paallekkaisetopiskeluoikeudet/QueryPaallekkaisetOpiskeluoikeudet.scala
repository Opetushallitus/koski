package fi.oph.koski.queuedqueries.paallekkaisetopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.queuedqueries.QueryUtils.{QueryResourceManager, defaultOrganisaatio}
import fi.oph.koski.queuedqueries.{QueryFormat, QueryParameters, QueryResultWriter}
import fi.oph.koski.raportit.{AikajaksoRaporttiRequest, RaportitAccessResolver, RaportitService}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.scalaschema.annotation.EnumValue

import java.time.LocalDate
import scala.util.Using

case class QueryPaallekkaisetOpiskeluoikeudet(
  @EnumValue("paallekkaisetOpiskeluoikeudet")
  `type`: String = "paallekkaisetOpiskeluoikeudet",
  @EnumValue(QueryFormat.csv)
  @EnumValue(QueryFormat.xlsx)
  format: String,
  organisaatioOid: Option[Organisaatio.Oid] = None,
  language: Option[String] = None,
  alku: LocalDate,
  loppu: LocalDate,
) extends QueryParameters with Logging {
  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] =
    QueryResourceManager(logger) { mgr =>
      implicit val manager: Using.Manager = mgr

      val raportitService = new RaportitService(application)

      val request = AikajaksoRaporttiRequest(
        oppilaitosOid = organisaatioOid.get,
        downloadToken = None,
        password = "", // TODO: Salasanan arpominen ja palauttaminen responsessa
        alku = alku,
        loppu = loppu,
        lang = language.get,
      )

      val localizationReader = new LocalizationReader(application.koskiLocalizationRepository, language.get)
      writer.putReport(raportitService.paallekkaisetOpiskeluoikeudet(request, localizationReader), format, localizationReader)

      auditLog
    }

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
