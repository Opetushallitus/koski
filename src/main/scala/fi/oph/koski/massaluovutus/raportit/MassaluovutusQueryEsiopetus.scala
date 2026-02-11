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
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.MassaluovutusUtils.{QueryResourceManager, defaultOrganisaatio, generatePassword}
import fi.oph.koski.massaluovutus.{KoulutuksenjärjestäjienMassaluovutusQueryParameters, QueryFormat, QueryMeta, QueryResultWriter}
import fi.oph.koski.raportit.RaportitService
import fi.oph.koski.raportit.esiopetus.EsiopetusRaporttiService
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Using

@Title("Esiopetus")
@Description("Palauttaa esiopetuksen raportin.")
case class MassaluovutusQueryEsiopetus(
  @EnumValues(Set("esiopetus"))
  `type`: String = "esiopetus",
  @EnumValues(Set(QueryFormat.xlsx))
  format: String = QueryFormat.xlsx,
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Päivämäärä, jolta raportti lasketaan.")
  paiva: LocalDate,
  @Description("Kotikuntapäivämäärä historialliseen kotikuntahakuun.")
  kotikuntaPvm: Option[LocalDate] = None,
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends KoulutuksenjärjestäjienMassaluovutusQueryParameters with Logging {

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit] =
    QueryResourceManager(logger) { mgr =>
      implicit val manager: Using.Manager = mgr
      implicit val s: KoskiSpecificSession = user.asInstanceOf[KoskiSpecificSession]

      val esiopetusService = new EsiopetusRaporttiService(application)
      val raportitService = new RaportitService(application)

      val localizationReader = new LocalizationReader(application.koskiLocalizationRepository, language.get)
      val pw = password.getOrElse(generatePassword(16))

      val response = esiopetusService.buildOrganisaatioRaportti(
        organisaatioOid = organisaatioOid.get,
        date = paiva,
        kotikuntaPäivänä = kotikuntaPvm,
        password = pw,
        downloadToken = None,
        t = localizationReader
      )

      writer.putReport(response, format, localizationReader)
      writer.patchMeta(QueryMeta(password = Some(pw)))

      writer.patchMeta(QueryMeta(
        raportointikantaGeneratedAt = Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika),
      ))

      auditLog
    }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = withKoskiSpecificSession { u =>
    (u.hasGlobalReadAccess || organisaatioOid.exists(oid => u.hasRaporttiReadAccess(oid))) &&
      u.allowedOpiskeluoikeudetJaPäätasonSuoritukset.intersects(OoPtsMask(OpiskeluoikeudenTyyppi.esiopetus.koodiarvo))
  }

  override def fillAndValidate(implicit user: Session): Either[HttpStatus, MassaluovutusQueryEsiopetus] =
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
        "raportti" -> List("esiopetus"),
        "oppilaitosOid" -> organisaatioOid.toList,
        "paiva" -> List(paiva.format(DateTimeFormatter.ISO_DATE)),
        "kotikuntaPvm" -> kotikuntaPvm.map(_.format(DateTimeFormatter.ISO_DATE)).toList,
        "lang" -> language.toList,
      ).filter(_._2.nonEmpty)))))
}

object QueryEsiopetusDocumentation {
  def xlsxExample: MassaluovutusQueryEsiopetus = MassaluovutusQueryEsiopetus(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    language = Some("fi"),
    paiva = LocalDate.of(2024, 1, 15),
    password = Some("hunter2"),
  )
}
