package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{DataSheet, OppilaitosRaporttiResponse, WorkbookSettings}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.koski.valpas.{ValpasAccessResolver, ValpasErrorCategory}

import java.time.format.DateTimeFormatter

class ValpasRouhintaService(application: KoskiApplication) {
  private val heturouhinta = new ValpasHeturouhintaService(application)
  private val kuntarouhinta = new ValpasKuntarouhintaService(application)
  private val localization = application.valpasLocalizationRepository
  private val accessResolver = new ValpasAccessResolver
  private val rajapäivät = application.valpasRajapäivätService

  def haeHetulistanPerusteella
  (hetut: Seq[String])
  (implicit session: ValpasSession)
  : Either[HttpStatus, HeturouhinnanTulos] = {
    if (accessResolver.accessToAnyOrg(ValpasRooli.KUNTA)) {
      heturouhinta.haeHetulistanPerusteella(hetut)
    } else {
      Left(ValpasErrorCategory.forbidden.toiminto())
    }
  }

  def haeHetulistanPerusteellaExcel
    (hetut: Seq[String], language: String, password: Option[String])
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppilaitosRaporttiResponse] = {
    val t = new LocalizationReader(localization, language)
    haeHetulistanPerusteella(hetut)
      .map(ValpasHeturouhintaSheets(_, t).build())
      .map(asOppilaitosRaporttiResponse(
        title = t.get("rouhinta_hetulista_otsikko"),
        filename = t.get("rouhinta_hetulista_tiedostonimi", Map("pvm" -> rajapäivät.tarkastelupäivä.format(DateTimeFormatter.ISO_DATE))),
        password = password,
      ))
  }

  def haeKunnanPerusteella
    (kunta: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, KuntarouhinnanTulos] = {
    if (accessResolver.accessToKuntaOrg(kunta)) {
      kuntarouhinta.haeKunnanPerusteella(kunta)
    } else {
      Left(ValpasErrorCategory.forbidden.toiminto())
    }
  }

  def haeKunnanPerusteellaExcel
    (kunta: String, language: String, password: Option[String])
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppilaitosRaporttiResponse] = {
    val t = new LocalizationReader(localization, language)
    haeKunnanPerusteella(kunta)
      .map(ValpasKuntarouhintaSheets(_, t).build())
      .map(asOppilaitosRaporttiResponse(
        title = t.get("rouhinta_kunta_otsikko"),
        filename = t.get("rouhinta_kunta_tiedostonimi", Map("pvm" -> rajapäivät.tarkastelupäivä.format(DateTimeFormatter.ISO_DATE))),
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
