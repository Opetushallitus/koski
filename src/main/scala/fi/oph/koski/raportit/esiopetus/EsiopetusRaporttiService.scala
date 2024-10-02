package fi.oph.koski.raportit.esiopetus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.raportit.{DataSheet, OppilaitosRaporttiResponse, WorkbookSettings}
import fi.oph.koski.schema.Organisaatio.Oid

import java.time.LocalDate

class EsiopetusRaporttiService(application: KoskiApplication) {
  private val esiopetusRaportti = EsiopetusRaportti(application.raportointiDatabase.db, application.organisaatioService)

  def buildOstopalveluRaportti(
    date: LocalDate,
    kotikuntaPäivänä: Option[LocalDate],
    password: String,
    downloadToken: Option[String],
    t: LocalizationReader
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    val ostopalveluOrganisaatiot = omatOstopalveluOrganisaatioOidit
    auditLog(date, kotikuntaPäivänä, session, ostopalveluOrganisaatiot.mkString(","), t.language)
    buildRaportti(
      date,
      kotikuntaPäivänä,
      password,
      downloadToken,
      ostopalveluOrganisaatiot,
      filename(
        t.get("raportti-excel-esiopetus-tiedoston-etuliite"),
        t.get("raportti-excel-esiopetus-ostopalvelu-tiedoston-etuliite"),
        date
      ),
      t
    )
  }

  def buildOrganisaatioRaportti(
    organisaatioOid: Oid,
    date: LocalDate,
    kotikuntaPäivänä: Option[LocalDate],
    password: String,
    downloadToken: Option[String],
    t: LocalizationReader
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    auditLog(date, kotikuntaPäivänä, session, organisaatioOid, t.language)
    buildRaportti(
      date,
      kotikuntaPäivänä,
      password,
      downloadToken,
      List(organisaatioOid),
      filename(t.get("raportti-excel-esiopetus-tiedoston-etuliite"), organisaatioOid, date),
      t
    )
  }

  private def auditLog(date: LocalDate, kotikuntaDate: Option[LocalDate], session: KoskiSpecificSession, organisaatio: String, lang: String): Unit = {
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=esiopetus&oppilaitosOid=$organisaatio&paiva=$date&lang=$lang${kotikuntaDate.fold("")(p => s"&kotikuntaPvm=$p")}")))
  }

  private def buildRaportti(
    date: LocalDate,
    kotikuntaPäivänä: Option[LocalDate],
    password: String,
    downloadToken: Option[String],
    oppilaitokset: List[Oid],
    filename: String,
    t: LocalizationReader
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse =
    OppilaitosRaporttiResponse(
      sheets = buildRaportti(date, kotikuntaPäivänä, oppilaitokset, t),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-esiopetus-title"), Some(password)),
      filename = filename,
      downloadToken = downloadToken
    )

  private def buildRaportti(
    date: LocalDate,
    kotikuntaPäivänä: Option[LocalDate],
    oppilaitokset: List[Oid],
    t: LocalizationReader
  )(implicit session: KoskiSpecificSession): Seq[DataSheet] = {
    Seq(esiopetusRaportti.build(oppilaitokset, date, kotikuntaPäivänä, t))
  }

  private def filename(etuliite: String, oppilaitos: String, date: LocalDate): String = {
    s"${etuliite}_${oppilaitos}_${date.toString.replaceAll("-", "")}.xlsx"
  }

  private def omatOstopalveluOrganisaatioOidit(implicit session: KoskiSpecificSession): List[Oid] =
    application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)
}

