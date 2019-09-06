package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.KoskiMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.organisaatio.OrganisaatioOid
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.scalatra.{ContentEncodingSupport, Cookie, CookieOptions}

class RaportitServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ContentEncodingSupport {

  private lazy val raportitService = new RaportitService(application)
  private lazy val accessResolver = RaportitAccessResolver(application)

  before() {
    if (!application.raportointikantaService.isAvailable) {
      haltWithStatus(KoskiErrorCategory.unavailable.raportit())
    }
  }

  get("/mahdolliset-raportit/:oppilaitosOid") {
    val oid = getOppilaitosParamAndCheckAccess
    accessResolver.mahdollisetRaporttienTyypitOrganisaatiolle(oid).map(_.toString)
  }

  get("/ammatillinenopiskelijavuositiedot") {
    val parsedRequest = parseAikajaksoRaporttiRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=ammatillinenopiskelijavuositiedot&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    excelResponse(raportitService.opiskelijaVuositiedot(parsedRequest))
  }

  get("/ammatillinentutkintosuoritustietojentarkistus") {
    val parsedRequest = parseAmmatillinenSuoritusTiedotRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=ammatillinentutkintosuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    excelResponse(raportitService.ammatillinenTutkintoSuoritustietojenTarkistus(parsedRequest))
  }

  get("/ammatillinenosittainensuoritustietojentarkistus") {
    val parsedRequest = parseAmmatillinenSuoritusTiedotRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=ammatillinenosittainensuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    excelResponse(raportitService.ammatillinenOsittainenSuoritustietojenTarkistus(parsedRequest))
  }

  get("/perusopetuksenvuosiluokka") {
    val parsedRequest = parseVuosiluokkaRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=perusopetuksenvuosiluokka&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}&vuosiluokka=${parsedRequest.vuosiluokka}")))
    excelResponse(raportitService.perusopetuksenVuosiluokka(parsedRequest))
  }

  get("/lukionsuoritustietojentarkistus") {
    val parsedRequest = parseAikajaksoRaporttiRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=lukionsuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    excelResponse(raportitService.lukioraportti(parsedRequest))
  }

  private def excelResponse(raportti: OppilaitosRaporttiResponse) = {
    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    response.setHeader("Content-Disposition", s"""attachment; filename="${raportti.filename}"""")
    raportti.downloadToken.foreach { t => response.addCookie(Cookie("koskiDownloadToken", t)(CookieOptions(path = "/", maxAge = 600))) }
    ExcelWriter.writeExcel(
      raportti.workbookSettings,
      raportti.sheets,
      response.getOutputStream
    )
  }

  private def parseAikajaksoRaporttiRequest: AikajaksoRaporttiRequest = {
    val oppilaitosOid = getOppilaitosParamAndCheckAccess
    val (alku, loppu) = getAlkuLoppuParams
    val password = getStringParam("password")
    val downloadToken = params.get("downloadToken")

    AikajaksoRaporttiRequest(oppilaitosOid, downloadToken, password, alku, loppu)
  }

  private def parseAmmatillinenSuoritusTiedotRequest: AmmatillinenSuoritusTiedotRequest = {
    val oppilaitosOid = getOppilaitosParamAndCheckAccess
    val (alku, loppu) = getAlkuLoppuParams
    val password = getStringParam("password")
    val downloadToken = params.get("downloadToken")
    val osasuoritustenAikarajaus = getBooleanParam("osasuoritustenAikarajaus")

    AmmatillinenSuoritusTiedotRequest(oppilaitosOid, downloadToken, password, alku, loppu, osasuoritustenAikarajaus)
  }

  private def parseVuosiluokkaRequest: PerusopetuksenVuosiluokkaRequest = {
   PerusopetuksenVuosiluokkaRequest(
     oppilaitosOid = getOppilaitosParamAndCheckAccess,
     downloadToken = params.get("downloadToken"),
     password = getStringParam("password"),
     paiva = getLocalDateParam("paiva"),
     vuosiluokka = getStringParam("vuosiluokka")
   )
  }

  private def getOppilaitosParamAndCheckAccess: Organisaatio.Oid = {
    if (!koskiSession.hasRaportitAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
    }
    val oppilaitosOid = OrganisaatioOid.validateOrganisaatioOid(getStringParam("oppilaitosOid")) match {
      case Left(error) => haltWithStatus(error)
      case Right(oid) if !koskiSession.hasReadAccess(oid) => haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
      case Right(oid) => oid
    }
    oppilaitosOid
  }

  private def getAlkuLoppuParams: (LocalDate, LocalDate) = {
    val alku = getLocalDateParam("alku")
    val loppu = getLocalDateParam("loppu")
    if (loppu.isBefore(alku)) {
      haltWithStatus(KoskiErrorCategory.badRequest.format.pvm("loppu ennen alkua"))
    }
    (alku, loppu)
  }

  private def getLocalDateParam(param: String): LocalDate = {
    try {
      LocalDate.parse(getStringParam(param))
    } catch {
      case e: DateTimeParseException => haltWithStatus(KoskiErrorCategory.badRequest.format.pvm())
    }
  }
}
