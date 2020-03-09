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
    auditLog(date, session, ostopalveluOrganisaatiot)
    buildRapsa(date, password, downloadToken, ostopalveluOrganisaatiot, filename("ostopalvelu_tai_palveluseteli", date))
  }

  def buildRaportti(oppilaitos: Oid, date: LocalDate, password: String, downloadToken: Option[String])(implicit session: KoskiSession): OppilaitosRaporttiResponse = {
    auditLog(date, session, List(oppilaitos))
    buildRapsa(date, password, downloadToken, List(oppilaitos), filename(oppilaitos, date))
  }

  private def auditLog(date: LocalDate, session: KoskiSession, organisaatiot: List[String]) =
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=esiopetus&oppilaitosOid=${organisaatiot.mkString(",")}&paiva=$date")))

  private def buildRapsa(date: LocalDate, password: String, downloadToken: Option[String], oppilaitokset: List[Oid], filename: String)(implicit session: KoskiSession): OppilaitosRaporttiResponse =
    OppilaitosRaporttiResponse(
      sheets = buildRaportti(date, oppilaitokset),
      workbookSettings = WorkbookSettings("Esiopetus", Some(password)),
      filename = filename,
      downloadToken = downloadToken
    )

  private def buildRaportti(date: LocalDate, oppilaitokset: List[Oid])(implicit session: KoskiSession): Seq[DataSheet] = {
    Seq(esiopetusRaportti.build(oppilaitokset, Date.valueOf(date)))
  }

  private def filename(oppilaitos: String, date: LocalDate): String = {
    s"esiopetus_koski_raportti_${oppilaitos}_${date.toString.replaceAll("-","")}.xlsx"
  }

  private def omatOstopalveluOrganisaatioOidit(implicit session: KoskiSession): List[Oid] =
    application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)
}

