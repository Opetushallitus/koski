package fi.oph.koski.massaluovutus.raportit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.{KoskiSpecificSession, OoPtsMask, Session}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.massaluovutus.MassaluovutusUtils.{QueryResourceManager, defaultOrganisaatio, generatePassword}
import fi.oph.koski.massaluovutus.{KoulutuksenjärjestäjienMassaluovutusQueryParameters, QueryMeta, QueryResultWriter}
import fi.oph.koski.raportit.{OppilaitosRaporttiResponse, RaportitService}
import fi.oph.koski.schema.{Koodistokoodiviite, Organisaatio}

import scala.util.Using

/**
 * Base trait for massaluovutus report queries. Provides common implementations for:
 * - queryAllowed (authorization check)
 * - fillAndValidate (parameter validation and defaults)
 * - run (report generation boilerplate)
 * - audit logging
 *
 * Each report class extends this trait and provides:
 * - opiskeluoikeudenTyyppi: The education type for authorization
 * - raporttiName: Name used in audit logging
 * - auditLogParams: Report-specific audit log parameters
 * - generateReport: The actual report generation logic
 * - withFilledParams: Creates a copy with filled organisaatioOid and language
 */
trait MassaluovutusRaporttiBase[Self <: MassaluovutusRaporttiBase[Self]]
  extends KoulutuksenjärjestäjienMassaluovutusQueryParameters with Logging {
  this: Self =>

  // Common fields that all reports must have
  def organisaatioOid: Option[Organisaatio.Oid]
  def language: Option[String]
  def password: Option[String]
  def format: String

  // Abstract methods that each report implements
  def opiskeluoikeudenTyyppi: Koodistokoodiviite
  def raporttiName: String
  def auditLogParams: Map[String, List[String]]

  protected def generateReport(
    raportitService: RaportitService,
    localizationReader: LocalizationReader,
    pw: String
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse

  protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String): Self

  // Common queryAllowed implementation
  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean =
    withKoskiSpecificSession { u =>
      (u.hasGlobalReadAccess || organisaatioOid.exists(oid => u.hasRaporttiReadAccess(oid))) &&
        u.allowedOpiskeluoikeudetJaPäätasonSuoritukset.intersects(OoPtsMask(opiskeluoikeudenTyyppi.koodiarvo))
    }

  // Common fillAndValidate implementation
  override def fillAndValidate(implicit user: Session): Either[HttpStatus, Self] =
    for {
      orgOid <- organisaatioOid
        .toRight(defaultOrganisaatio)
        .fold(identity, Right.apply)
      lang <- Right(language.getOrElse(user.lang))
    } yield withFilledParams(orgOid, lang)

  // Common run implementation
  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit] =
    QueryResourceManager(logger) { mgr =>
      implicit val manager: Using.Manager = mgr
      implicit val session: KoskiSpecificSession = user.asInstanceOf[KoskiSpecificSession]

      val raportitService = new RaportitService(application)
      val localizationReader = new LocalizationReader(application.koskiLocalizationRepository, language.get)
      val pw = password.getOrElse(generatePassword(16))

      val response = generateReport(raportitService, localizationReader, pw)
      writer.putReport(response, format, localizationReader)
      writer.patchMeta(QueryMeta(password = Some(pw)))
      writer.patchMeta(QueryMeta(
        raportointikantaGeneratedAt = Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika),
      ))

      doAuditLog
    }

  private def doAuditLog(implicit user: Session): Unit =
    AuditLog.log(KoskiAuditLogMessage(
      OPISKELUOIKEUS_RAPORTTI,
      user,
      Map(hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(
        (Map(
          "raportti" -> List(raporttiName),
          "oppilaitosOid" -> organisaatioOid.toList,
          "lang" -> language.toList
        ) ++ auditLogParams).filter(_._2.nonEmpty)
      ))))
}
