package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.AuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.schema.Organisaatio.Oid

class EsiopetusRaporttiService(application: KoskiApplication) {
  private val esiopetusRaportti = EsiopetusRaportti(application.raportointiDatabase.db, application.organisaatioService)

  def buildOstopalveluRaportti(date: LocalDate, password: String, downloadToken: Option[String])(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    val ostopalveluOrganisaatiot = omatOstopalveluOrganisaatioOidit
    auditLog(date, session, ostopalveluOrganisaatiot.mkString(","))
    buildRaportti(date, password, downloadToken, ostopalveluOrganisaatiot, filename("ostopalvelu_tai_palveluseteli", date))
  }

  def buildOrganisaatioRaportti(organisaatioOid: Oid, date: LocalDate, password: String, downloadToken: Option[String])(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    auditLog(date, session, organisaatioOid)
    buildRaportti(date, password, downloadToken, List(organisaatioOid), filename(organisaatioOid, date))
  }

  private def auditLog(date: LocalDate, session: KoskiSpecificSession, organisaatio: String) = {
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=esiopetus&oppilaitosOid=$organisaatio&paiva=$date")))
  }

  private def buildRaportti(date: LocalDate, password: String, downloadToken: Option[String], oppilaitokset: List[Oid], filename: String)(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse =
    OppilaitosRaporttiResponse(
      sheets = buildRaportti(date, oppilaitokset),
      workbookSettings = WorkbookSettings("Esiopetus", Some(password)),
      filename = filename,
      downloadToken = downloadToken
    )

  private def buildRaportti(date: LocalDate, oppilaitokset: List[Oid])(implicit session: KoskiSpecificSession): Seq[DataSheet] = {
    Seq(esiopetusRaportti.build(oppilaitokset, date))
  }

  private def organisaationAlaisetOrganisaatiot(organisaatioOid: Oid)(implicit user: KoskiSpecificSession) = {
    application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid)
  }

  private def filename(oppilaitos: String, date: LocalDate): String = {
    s"esiopetus_koski_raportti_${oppilaitos}_${date.toString.replaceAll("-","")}.xlsx"
  }

  private def omatOstopalveluOrganisaatioOidit(implicit session: KoskiSpecificSession): List[Oid] =
    application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)
}

