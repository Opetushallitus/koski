package fi.oph.koski.queuedqueries.paallekkaisetopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.queuedqueries.QueryUtils.{QueryResourceManager, defaultOrganisaatio, generatePassword}
import fi.oph.koski.queuedqueries.{QueryFormat, QueryMeta, QueryParameters, QueryResultWriter}
import fi.oph.koski.raportit.{AikajaksoRaporttiRequest, RaportitService}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import scala.util.Using

@Title("Päällekkäiset opiskeluoikeudet")
@Description("Palauttaa hakuehtojen mukaiset organisaation ja sen alaorganisaatioiden päällekkäiset opiskeluoikeudet.")
@Description("Saatu tulostiedosto vastaa raporttinäkymästä ladattavaa tiedostoa, mutta se on mahdollista ladata myös paremmin koneluettavassa csv-muodossa.")
case class QueryPaallekkaisetOpiskeluoikeudet(
  @EnumValues(Set("paallekkaisetOpiskeluoikeudet"))
  `type`: String = "paallekkaisetOpiskeluoikeudet",
  @EnumValues(Set(QueryFormat.csv, QueryFormat.xlsx))
  format: String,
  @Description("Kyselyyn otettavan organisaation oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli. CSV-muodossa vaikuttaa vain organisaatioiden nimiin.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Tutkittavan aikajakson alkamispäivä.")
  alku: LocalDate,
  @Description("Tutkittavan aikajakson päättymispäivä.")
  loppu: LocalDate,
  @Description("Salasana. Merkityksellinen vain xlsx-tiedostoille. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends QueryParameters with Logging {
  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] =
    QueryResourceManager(logger) { mgr =>
      implicit val manager: Using.Manager = mgr

      val raportitService = new RaportitService(application)

      val request = AikajaksoRaporttiRequest(
        oppilaitosOid = organisaatioOid.get,
        downloadToken = None,
        password = password.getOrElse(generatePassword(16)),
        alku = alku,
        loppu = loppu,
        lang = language.get,
      )

      val localizationReader = new LocalizationReader(application.koskiLocalizationRepository, language.get)
      writer.putReport(raportitService.paallekkaisetOpiskeluoikeudet(request, localizationReader), format, localizationReader)
      writer.patchMeta(QueryMeta(password = Some(request.password)))

      auditLog
    }

  override def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasGlobalReadAccess || organisaatioOid.exists(user.organisationOids(AccessType.read).contains)

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

  private def auditLog(implicit user: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(
      OPISKELUOIKEUS_RAPORTTI,
      user,
      Map(hakuEhto -> s"raportti=paallekkaisetopiskeluoikeudet&oppilaitosOid=${organisaatioOid}&alku=${alku}&loppu=${loppu}&lang=${language.get}")))
}

object QueryPaallekkaisetOpiskeluoikeudetDocumentation {
  def csvExample: QueryPaallekkaisetOpiskeluoikeudet = QueryPaallekkaisetOpiskeluoikeudet(
    format = QueryFormat.csv,
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    language = Some("fi"),
    alku = LocalDate.of(2024, 1, 1),
    loppu = LocalDate.of(2024, 3, 31),
  )

  def xlsxExample: QueryPaallekkaisetOpiskeluoikeudet = QueryPaallekkaisetOpiskeluoikeudet(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    language = Some("fi"),
    alku = LocalDate.of(2024, 1, 1),
    loppu = LocalDate.of(2024, 3, 31),
    password = Some("hunter2"),
  )
}
