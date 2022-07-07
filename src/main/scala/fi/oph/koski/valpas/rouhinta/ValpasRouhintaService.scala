package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{DataSheet, OppilaitosRaporttiResponse, WorkbookSettings}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.koski.valpas.oppija.{ValpasAccessResolver, ValpasErrorCategory}

import java.time.format.DateTimeFormatter

class ValpasRouhintaService(application: KoskiApplication) extends ValpasRouhintaTiming {
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
      heturouhinta.haeHetulistanPerusteellaIlmanOikeustarkastusta(hetut)
    } else {
      Left(ValpasErrorCategory.forbidden.toiminto())
    }
  }

  def haeHetulistanPerusteellaExcel
    (hetut: Seq[String], language: String, password: Option[String])
    (implicit session: ValpasSession)
  : Either[HttpStatus, HeturouhinnanExcelTulos] = {
    haeHetulistanPerusteella(hetut)
      .map(asHeturouhintaExcelResponse(language, password, hetut.size))
  }

  def haeKunnanPerusteella
    (kunta: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, KuntarouhinnanTulos] = {
    if (accessResolver.accessToKuntaOrg(kunta)) {
      kuntarouhinta.haeKunnanPerusteellaIlmanOikeustarkastusta(kunta)
    } else {
      Left(ValpasErrorCategory.forbidden.toiminto())
    }
  }

  def haeKunnanPerusteellaExcel
    (kunta: String, language: String, password: Option[String])
    (implicit session: ValpasSession)
  : Either[HttpStatus, KuntarouhinnanExcelTulos] = {
    haeKunnanPerusteella(kunta)
      .map(asKuntarouhintaExcelResponse(language, password))
  }

  private def asHeturouhintaExcelResponse
    (language: String, password: Option[String], inputSize: Int)
    (tulos: HeturouhinnanTulos)
  : HeturouhinnanExcelTulos = {
    rouhintaTimed("asExcelResponse", inputSize) {
      val t = new LocalizationReader(localization, language)
      val sheets = ValpasHeturouhintaSheets(tulos, t).build()
      val response = asOppilaitosRaporttiResponse(
        title = t.get("rouhinta_hetulista_otsikko"),
        filename = t.get("rouhinta_hetulista_tiedostonimi", Map("pvm" -> rajapäivät.tarkastelupäivä.format(DateTimeFormatter.ISO_DATE))),
        password = password,
      )(sheets)
      HeturouhinnanExcelTulos(tulos, response)
    }
  }

  private def asKuntarouhintaExcelResponse
    (language: String, password: Option[String])
    (tulos: KuntarouhinnanTulos) =
  {
    rouhintaTimed("asExcelResponse", tulos.eiOppivelvollisuuttaSuorittavat.size) {
      val t = new LocalizationReader(localization, language)
      val sheets = ValpasKuntarouhintaSheets(tulos, t).build()
      val response = asOppilaitosRaporttiResponse(
        title = t.get("rouhinta_kunta_otsikko"),
        filename = t.get("rouhinta_kunta_tiedostonimi", Map("pvm" -> rajapäivät.tarkastelupäivä.format(DateTimeFormatter.ISO_DATE))),
        password = password,
      )(sheets)
      KuntarouhinnanExcelTulos(tulos, response)
    }
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

case class HeturouhinnanExcelTulos(
  data: HeturouhinnanTulos,
  response: OppilaitosRaporttiResponse,
)

case class KuntarouhinnanExcelTulos(
  data: KuntarouhinnanTulos,
  response: OppilaitosRaporttiResponse,
)
