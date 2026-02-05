package fi.oph.koski.massaluovutus.raportit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.{OoPtsMask, Session}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.MassaluovutusUtils.{QueryResourceManager, defaultOrganisaatio, generatePassword}
import fi.oph.koski.massaluovutus.{KoulutuksenjärjestäjienMassaluovutusQueryParameters, QueryFormat, QueryMeta, QueryResultWriter}
import fi.oph.koski.raportit.{AikuistenPerusopetusRaporttiRequest, RaportitService}
import fi.oph.koski.raportit.aikuistenperusopetus.{AikuistenPerusopetusAlkuvaiheRaportti, AikuistenPerusopetusOppiaineenOppimääräRaportti, AikuistenPerusopetusPäättövaiheRaportti, AikuistenPerusopetusRaporttiType}
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Using

@Title("Aikuisten perusopetuksen suoritustietojen tarkistus")
@Description("Palauttaa aikuisten perusopetuksen suoritustietojen tarkistusraportin.")
case class MassaluovutusQueryAikuistenPerusopetusSuoritustiedot(
  @EnumValues(Set("aikuistenPerusopetusSuoritustiedot"))
  `type`: String = "aikuistenPerusopetusSuoritustiedot",
  @EnumValues(Set(QueryFormat.xlsx))
  format: String,
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Tutkittavan aikajakson alkamispäivä.")
  alku: LocalDate,
  @Description("Tutkittavan aikajakson päättymispäivä.")
  loppu: LocalDate,
  @Description("Raportin tyyppi: alkuvaihe, päättövaihe tai oppiaineenoppimäärä.")
  @EnumValues(Set("alkuvaihe", "päättövaihe", "oppiaineenoppimäärä"))
  raportinTyyppi: String,
  @Description("Jos true, osasuorituksiin rajoitetaan vain aikajakson sisällä arvioidut.")
  osasuoritustenAikarajaus: Option[Boolean] = None,
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends KoulutuksenjärjestäjienMassaluovutusQueryParameters with Logging {

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit] =
    QueryResourceManager(logger) { mgr =>
      implicit val manager: Using.Manager = mgr

      val raportitService = new RaportitService(application)

      val raporttiType: AikuistenPerusopetusRaporttiType = raportinTyyppi match {
        case "alkuvaihe" => AikuistenPerusopetusAlkuvaiheRaportti
        case "päättövaihe" => AikuistenPerusopetusPäättövaiheRaportti
        case "oppiaineenoppimäärä" => AikuistenPerusopetusOppiaineenOppimääräRaportti
      }

      val request = AikuistenPerusopetusRaporttiRequest(
        oppilaitosOid = organisaatioOid.get,
        downloadToken = None,
        password = password.getOrElse(generatePassword(16)),
        alku = alku,
        loppu = loppu,
        osasuoritustenAikarajaus = osasuoritustenAikarajaus.getOrElse(false),
        raportinTyyppi = raporttiType,
        lang = language.get,
      )

      val localizationReader = new LocalizationReader(application.koskiLocalizationRepository, language.get)
      writer.putReport(raportitService.aikuistenPerusopetus(request, localizationReader), format, localizationReader)
      writer.patchMeta(QueryMeta(password = Some(request.password)))

      writer.patchMeta(QueryMeta(
        raportointikantaGeneratedAt = Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika),
      ))

      auditLog
    }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = withKoskiSpecificSession { u =>
    (u.hasGlobalReadAccess || organisaatioOid.exists(oid => u.hasRaporttiReadAccess(oid))) &&
      u.allowedOpiskeluoikeudetJaPäätasonSuoritukset.intersects(OoPtsMask(OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo))
  }

  override def fillAndValidate(implicit user: Session): Either[HttpStatus, MassaluovutusQueryAikuistenPerusopetusSuoritustiedot] =
    for {
      orgOid <- organisaatioOid
        .toRight(defaultOrganisaatio)
        .fold(identity, Right.apply)
      lang <- Right(language.getOrElse(user.lang))
    } yield copy(
      organisaatioOid = Some(orgOid),
      language = Some(lang),
    )

  private def auditLog(implicit user: Session): Unit =
    AuditLog.log(KoskiAuditLogMessage(
      OPISKELUOIKEUS_RAPORTTI,
      user,
      Map(hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(Map(
        "raportti" -> List("aikuistenperusopetuksensuoritustietojentarkistus"),
        "oppilaitosOid" -> organisaatioOid.toList,
        "alku" -> List(alku.format(DateTimeFormatter.ISO_DATE)),
        "loppu" -> List(loppu.format(DateTimeFormatter.ISO_DATE)),
        "raportinTyyppi" -> List(raportinTyyppi),
        "osasuoritustenAikarajaus" -> osasuoritustenAikarajaus.map(_.toString).toList,
        "lang" -> language.toList,
      ).filter(_._2.nonEmpty)))))
}

object QueryAikuistenPerusopetusSuoritustiedotDocumentation {
  def xlsxExample: MassaluovutusQueryAikuistenPerusopetusSuoritustiedot = MassaluovutusQueryAikuistenPerusopetusSuoritustiedot(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    language = Some("fi"),
    alku = LocalDate.of(2024, 1, 1),
    loppu = LocalDate.of(2024, 3, 31),
    raportinTyyppi = "päättövaihe",
    password = Some("hunter2"),
  )
}
