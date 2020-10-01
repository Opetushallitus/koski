package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, AuditLogMessage}
import fi.oph.koski.schema.Organisaatio.Oid

class EsiopetusRaporttiService(application: KoskiApplication) {
  private val esiopetusRaportti = EsiopetusRaportti(application.raportointiDatabase.db, application.organisaatioService)

  def buildOstopalveluRaportti(date: LocalDate, password: String, downloadToken: Option[String])(implicit session: KoskiSession): OppilaitosRaporttiResponse = {
    val ostopalveluOrganisaatiot = omatOstopalveluOrganisaatioOidit
    auditLog(date, session, ostopalveluOrganisaatiot.mkString(","))
    buildRaportti(date, password, downloadToken, ostopalveluOrganisaatiot, filename("ostopalvelu_tai_palveluseteli", date))
  }

  def buildOrganisaatioRaportti(organisaatioOid: Oid, date: LocalDate, password: String, downloadToken: Option[String])(implicit session: KoskiSession): OppilaitosRaporttiResponse = {
    auditLog(date, session, organisaatioOid)
    buildRaportti(date, password, downloadToken, List(organisaatioOid), filename(organisaatioOid, date))
  }

  private def auditLog(date: LocalDate, session: KoskiSession, organisaatio: String) = {
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=esiopetus&oppilaitosOid=$organisaatio&paiva=$date")))
  }

  private def buildRaportti(date: LocalDate, password: String, downloadToken: Option[String], oppilaitokset: List[Oid], filename: String)(implicit session: KoskiSession): OppilaitosRaporttiResponse =
    OppilaitosRaporttiResponse(
      sheets = buildRaportti(date, oppilaitokset),
      workbookSettings = WorkbookSettings("Esiopetus", Some(password)),
      filename = filename,
      downloadToken = downloadToken
    )

  private def buildRaportti(date: LocalDate, oppilaitokset: List[Oid])(implicit session: KoskiSession): Seq[DataSheet] = {
    Seq(esiopetusRaportti.build(oppilaitokset, Date.valueOf(date)))
  }

  private def organisaationAlaisetOrganisaatiot(organisaatioOid: Oid)(implicit user: KoskiSession) = {
    application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid)
  }

  private def filename(oppilaitos: String, date: LocalDate): String = {
    s"esiopetus_koski_raportti_${oppilaitos}_${date.toString.replaceAll("-","")}.xlsx"
  }

  private def omatOstopalveluOrganisaatioOidit(implicit session: KoskiSession): List[Oid] =
    application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)
}

