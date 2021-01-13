package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.common.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.common.log.KoskiMessageField.hakuEhto
import fi.oph.common.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.common.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.raportit.aikuistenperusopetus.AikuistenPerusopetusRaportti
import fi.oph.koski.organisaatio.{Kaikki, OrganisaatioHierarkia, OrganisaatioOid}
import fi.oph.koski.schema.{Koodistokoodiviite, OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.scalatra.{ContentEncodingSupport, Cookie, CookieOptions}

class RaportitServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ContentEncodingSupport {
  private lazy val raportitService = new RaportitService(application)
  private lazy val organisaatioService = application.organisaatioService
  private lazy val esiopetusService = new EsiopetusRaporttiService(application)
  private lazy val accessResolver = RaportitAccessResolver(application)

  before() {
    if (!application.raportointikantaService.isAvailable) {
      haltWithStatus(KoskiErrorCategory.unavailable.raportit())
    }
    if (!koskiSession.hasRaportitAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
    }
  }

  get("/organisaatiot") {
    organisaatioService.kaikkiKäyttöoikeudellisetOrganisaatiot
  }

  get("/organisaatiot-ja-raporttityypit") {
    raportitService.getRaportinOrganisatiotJaRaporttiTyypit(organisaatioService.kaikkiKäyttöoikeudellisetOrganisaatiot)
  }

  get("/paivitysaika") {
    raportitService.viimeisinPäivitys
  }

  // TODO: Tarpeeton kun uusi raporttikäli saadaan käyttöön, voi poistaa
  get("/mahdolliset-raportit/:oppilaitosOid") {
    getStringParam("oppilaitosOid") match {
      case organisaatioService.ostopalveluRootOid => Set(EsiopetuksenRaportti.toString, EsiopetuksenOppijaMäärienRaportti.toString)
      case oid: String => accessResolver.mahdollisetRaporttienTyypitOrganisaatiolle(validateOrganisaatioOid(oid)).map(_.toString)
    }
  }

  get("/paallekkaisetopiskeluoikeudet") {
    val req = parseAikajaksoRaporttiRequest
    auditLogRaportinLataus("paallekkaisetopiskeluoikeudet", req)
    writeExcel(raportitService.paallekkaisetOpiskeluoikeudet(req))
  }

  get("/ammatillinenopiskelijavuositiedot") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=ammatillinenopiskelijavuositiedot&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    writeExcel(raportitService.opiskelijaVuositiedot(parsedRequest))
  }

  get("/ammatillinentutkintosuoritustietojentarkistus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiAikarajauksellaRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=ammatillinentutkintosuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    writeExcel(raportitService.ammatillinenTutkintoSuoritustietojenTarkistus(parsedRequest))
  }

  get("/ammatillinenosittainensuoritustietojentarkistus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiAikarajauksellaRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=ammatillinenosittainensuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    writeExcel(raportitService.ammatillinenOsittainenSuoritustietojenTarkistus(parsedRequest))
  }

  get("/perusopetuksenvuosiluokka") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.perusopetus)
    val parsedRequest = parseVuosiluokkaRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=perusopetuksenvuosiluokka&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}&vuosiluokka=${parsedRequest.vuosiluokka}")))
    writeExcel(raportitService.perusopetuksenVuosiluokka(parsedRequest))
  }

  get("/lukionsuoritustietojentarkistus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.lukiokoulutus)
    val parsedRequest = parseAikajaksoRaporttiAikarajauksellaRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=lukionsuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    writeExcel(raportitService.lukioraportti(parsedRequest))
  }

  get("/lukiokurssikertymat") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.lukiokoulutus)
    val r = parseAikajaksoRaporttiRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=lukiokurssikertymat&oppilaitosOid=${r.oppilaitosOid}&alku=${r.alku}&loppu=${r.loppu}")))
    writeExcel(raportitService.lukioKoulutuksenKurssikertyma(r))
  }

  get("/lukiodiaibinternationalopiskelijamaarat") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.lukiokoulutus)
    val r = parseRaporttiPäivältäRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=lukiodiaibinternationalopiskelijamaarat&oppilaitosOid=${r.oppilaitosOid}&paiva=${r.paiva}")))
    writeExcel(raportitService.lukioDiaIbInternationalOpiskelijaMaaratRaportti(r))
  }

  get("/luvaopiskelijamaarat") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.luva)
    val r = parseRaporttiPäivältäRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=luvaopiskelijamaarat&oppilaitosOid=${r.oppilaitosOid}&paiva=${r.paiva}")))
    writeExcel(raportitService.lukioonValmistavanKoulutuksenOpiskelijaMaaratRaportti(r))
  }

  get("/aikuisten-perusopetus-suoritustietojen-tarkistus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.aikuistenperusopetus)
    val parsedRequest = parseAikuistenPerusopetusRaporttiRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=aikuistenperusopetuksensuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    writeExcel(raportitService.aikuistenPerusopetus(parsedRequest))
  }


  get("/muuammatillinen") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=muuammatillinen&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    writeExcel(raportitService.muuAmmatillinen(parsedRequest))
  }

  get("/topksammatillinen") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=topksammatillinen&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    writeExcel(raportitService.topksAmmatillinen(parsedRequest))
  }

  get("/esiopetus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.esiopetus)
    val date = getLocalDateParam("paiva")
    val password = getStringParam("password")
    val token = params.get("downloadToken")

    val resp = getStringParam("oppilaitosOid") match {
      case organisaatioService.ostopalveluRootOid =>
        esiopetusService.buildOstopalveluRaportti(date, password, token)
      case oid =>
        esiopetusService.buildOrganisaatioRaportti(validateOrganisaatioOid(oid), date, password, token)
    }

    writeExcel(resp)
  }

  get("/esiopetuksenoppijamaaratraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.esiopetus)
    val parsedRequest = parseRaporttiPäivältäRequest

    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=esiopetuksenoppijamaaratraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}")))
    writeExcel(raportitService.esiopetuksenOppijamäärät(parsedRequest))
  }

  get("/aikuistenperusopetuksenoppijamaaratraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.aikuistenperusopetus)
    val parsedRequest = parseRaporttiPäivältäRequest

    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=aikuistenperusopetuksenoppijamaaratraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}")))
    writeExcel(raportitService.aikuistenperusopetuksenOppijamäärät(parsedRequest))
  }

  get("/aikuistenperusopetuksenkurssikertymaraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.aikuistenperusopetus)
    val parsedRequest = parseAikajaksoRaporttiRequest

    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=aikuistenperusopetuksenkurssikertymaraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    writeExcel(raportitService.aikuistenperusopetuksenKurssikertymä(parsedRequest))
  }

  get("/perusopetuksenoppijamaaratraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.perusopetus)
    val parsedRequest = parseRaporttiPäivältäRequest

    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=perusopetuksenoppijamaaratraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}")))
    writeExcel(raportitService.perusopetuksenOppijamäärät(parsedRequest))
  }

  get("/perusopetuksenlisaopetuksenoppijamaaratraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus)
    val parsedRequest = parseRaporttiPäivältäRequest

    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=perusopetuksenlisaopetuksenoppijamaaratraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}")))
    writeExcel(raportitService.perusopetuksenLisäopetuksenOppijamäärät(parsedRequest))
  }

  private def requireOpiskeluoikeudenKayttooikeudet(opiskeluoikeudenTyyppiViite: Koodistokoodiviite) = {
    if (!koskiSession.allowedOpiskeluoikeusTyypit.contains(opiskeluoikeudenTyyppiViite.koodiarvo)) {
      haltWithStatus(KoskiErrorCategory.forbidden.opiskeluoikeudenTyyppi())
    }
  }

  private def writeExcel(raportti: OppilaitosRaporttiResponse) = {
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
    val oppilaitosOid = getOppilaitosOid
    val (alku, loppu) = getAlkuLoppuParams
    val password = getStringParam("password")
    val downloadToken = params.get("downloadToken")

    AikajaksoRaporttiRequest(oppilaitosOid, downloadToken, password, alku, loppu)
  }

  private def parseRaporttiPäivältäRequest: RaporttiPäivältäRequest = {
    val paiva = getLocalDateParam("paiva")
    val password = getStringParam("password")
    val downloadToken = params.get("downloadToken")
    val oppilaitosOid = getOppilaitosOid

    RaporttiPäivältäRequest(oppilaitosOid, downloadToken, password, paiva)
  }

  private def parseAikajaksoRaporttiAikarajauksellaRequest: AikajaksoRaporttiAikarajauksellaRequest = {
    val oppilaitosOid = getOppilaitosOid
    val (alku, loppu) = getAlkuLoppuParams
    val password = getStringParam("password")
    val downloadToken = params.get("downloadToken")
    val osasuoritustenAikarajaus = getBooleanParam("osasuoritustenAikarajaus")

    AikajaksoRaporttiAikarajauksellaRequest(oppilaitosOid, downloadToken, password, alku, loppu, osasuoritustenAikarajaus)
  }

  private def parseAikuistenPerusopetusRaporttiRequest: AikuistenPerusopetusRaporttiRequest = {
    val (alku, loppu) = getAlkuLoppuParams
    val raportinTyyppi = AikuistenPerusopetusRaportti.makeRaporttiType(params.get("raportinTyyppi").getOrElse("")) match {
      case Right(raportinTyyppi) => raportinTyyppi
      case Left(error) => haltWithStatus(KoskiErrorCategory.badRequest.queryParam(s"Epäkelpo raportinTyyppi: ${error}"))
    }
    AikuistenPerusopetusRaporttiRequest(
      oppilaitosOid = getOppilaitosOid,
      downloadToken = params.get("downloadToken"),
      password = getStringParam("password"),
      alku = alku,
      loppu = loppu,
      osasuoritustenAikarajaus = getBooleanParam("osasuoritustenAikarajaus"),
      raportinTyyppi = raportinTyyppi
    )
  }

  private def parseVuosiluokkaRequest: PerusopetuksenVuosiluokkaRequest = {
   PerusopetuksenVuosiluokkaRequest(
     oppilaitosOid = getOppilaitosOid,
     downloadToken = params.get("downloadToken"),
     password = getStringParam("password"),
     paiva = getLocalDateParam("paiva"),
     vuosiluokka = getStringParam("vuosiluokka")
   )
  }

  private def getOppilaitosOid: Organisaatio.Oid = {
    validateOrganisaatioOid(getStringParam("oppilaitosOid"))
  }

  private def validateOrganisaatioOid(oppilaitosOid: String) =
    OrganisaatioOid.validateOrganisaatioOid(oppilaitosOid) match {
      case Left(error) => haltWithStatus(error)
      case Right(oid) if !koskiSession.hasReadAccess(oid) => haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
      case Right(oid) => oid
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

  private def auditLogRaportinLataus(raportti: String, request: RaporttiAikajaksoltaRequest) =
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> request.auditlogHakuehto(raportti))))
}

