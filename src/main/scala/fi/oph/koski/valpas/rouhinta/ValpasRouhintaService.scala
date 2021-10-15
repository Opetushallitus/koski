package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{DataSheet, OppilaitosRaporttiResponse, WorkbookSettings}
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasRouhintaService(application: KoskiApplication) {
  private val heturouhinta = new ValpasHeturouhintaService(application)
  private val localization = application.valpasLocalizationRepository

  def haeHetulistanPerusteella
  (hetut: Seq[String])
  (implicit session: ValpasSession)
  : Either[HttpStatus, HeturouhinnanTulos] = {
    heturouhinta.haeHetulistanPerusteella(hetut)
  }

  def haeHetulistanPerusteellaExcel
    (hetut: Seq[String], language: String, password: Option[String])
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppilaitosRaporttiResponse] = {
    val t = new LocalizationReader(localization, language)
    heturouhinta.haeHetulistanPerusteella(hetut)
      .map(ValpasHeturouhintaSheets(_, t).build())
      .map(asOppilaitosRaporttiResponse(
        title = t.get("rouhinta_hetulista_otsikko"),
        filename = t.get("rouhinta_hetulista_tiedostonimi"),
        password = password,
      ))
  }

  private def asOppilaitosRaporttiResponse(title: String, filename: String, password: Option[String])(sheets: Seq[DataSheet]) = {
    OppilaitosRaporttiResponse(
      sheets = sheets,
      workbookSettings = WorkbookSettings(title, password),
      filename = filename,
      downloadToken = None,
    )
  }
}
