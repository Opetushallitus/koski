package fi.oph.koski.raportit.esiopetus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{OppilaitosRaporttiResponse, WorkbookSettings}
import fi.oph.koski.schema.Organisaatio.Oid

import java.time.LocalDate

class EsiopetusRaporttiService(application: KoskiApplication) {
  private val esiopetusRaportti = EsiopetusRaportti(application.raportointiDatabase.db, application.organisaatioService)

  def raportti(
    organisaatioOid: Oid,
    date: LocalDate,
    kotikuntaPäivänä: Option[LocalDate],
    password: String,
    downloadToken: Option[String],
    t: LocalizationReader
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    val ostopalvelu = organisaatioOid == application.organisaatioService.ostopalveluRootOid
    val oppilaitokset =
      if (ostopalvelu) application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid) else List(organisaatioOid)
    val tiedostonimenOrganisaatio =
      if (ostopalvelu) t.get("raportti-excel-esiopetus-ostopalvelu-tiedoston-etuliite") else organisaatioOid

    OppilaitosRaporttiResponse(
      sheets = Seq(esiopetusRaportti.build(oppilaitokset, date, kotikuntaPäivänä, t)),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-esiopetus-title"), Some(password)),
      filename = s"${t.get("raportti-excel-esiopetus-tiedoston-etuliite")}_${tiedostonimenOrganisaatio}_${date.toString.replaceAll("-", "")}.xlsx",
      downloadToken = downloadToken
    )
  }
}
