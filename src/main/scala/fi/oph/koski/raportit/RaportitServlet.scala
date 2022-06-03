package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.format.DateTimeParseException
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.raportit.aikuistenperusopetus.AikuistenPerusopetusRaportti
import fi.oph.koski.organisaatio.{Kaikki, OrganisaatioHierarkia, OrganisaatioOid}
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString, OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.{ContentEncodingSupport, Cookie, CookieOptions}

class RaportitServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ContentEncodingSupport {
  private lazy val raportitService = new RaportitService(application)
  private lazy val organisaatioService = application.organisaatioService
  private lazy val esiopetusService = new EsiopetusRaporttiService(application)
  private lazy val accessResolver = RaportitAccessResolver(application)

  before() {
    if (!application.raportointikantaService.isAvailable) {
      haltWithStatus(KoskiErrorCategory.unavailable.raportit())
    }
    if (!session.hasRaportitAccess) {
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
    raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika
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
    val t = new LocalizationReader(application.koskiLocalizationRepository, req.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=paallekkaisetopiskeluoikeudet&oppilaitosOid=${req.oppilaitosOid}&alku=${req.alku}&loppu=${req.loppu}&lang=${req.lang}")))
    writeExcel(raportitService.paallekkaisetOpiskeluoikeudet(req, t), t)
  }

  get("/ammatillinenopiskelijavuositiedot") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=ammatillinenopiskelijavuositiedot&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.opiskelijaVuositiedot(parsedRequest, t), t)
  }

  get("/ammatillinentutkintosuoritustietojentarkistus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiAikarajauksellaRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=ammatillinentutkintosuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.ammatillinenTutkintoSuoritustietojenTarkistus(parsedRequest, t), t)
  }

  get("/ammatillinenosittainensuoritustietojentarkistus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiAikarajauksellaRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=ammatillinenosittainensuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.ammatillinenOsittainenSuoritustietojenTarkistus(parsedRequest, t), t)
  }

  get("/perusopetuksenvuosiluokka") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.perusopetus)
    val parsedRequest = parseVuosiluokkaRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=perusopetuksenvuosiluokka&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}&vuosiluokka=${parsedRequest.vuosiluokka}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.perusopetuksenVuosiluokka(parsedRequest, t), t)
  }

  get("/lukionsuoritustietojentarkistus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.lukiokoulutus)
    val parsedRequest = parseAikajaksoRaporttiAikarajauksellaRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=lukionsuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.lukioraportti(parsedRequest, t), t)
  }

  get("/lukio2019suoritustietojentarkistus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.lukiokoulutus)
    val parsedRequest = parseAikajaksoRaporttiAikarajauksellaRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=lukio2019suoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.lukioraportti2019(parsedRequest, t), t)
  }

  get("/lukiokurssikertymat") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.lukiokoulutus)
    val r = parseAikajaksoRaporttiRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, r.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=lukiokurssikertymat&oppilaitosOid=${r.oppilaitosOid}&alku=${r.alku}&loppu=${r.loppu}&lang=${r.lang}")))
    writeExcel(raportitService.lukioKoulutuksenKurssikertyma(r, t), t)
  }

  get("/lukio2019opintopistekertymat") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.lukiokoulutus)
    val r = parseAikajaksoRaporttiRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, r.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=lukio2019opintopistekertymat&oppilaitosOid=${r.oppilaitosOid}&alku=${r.alku}&loppu=${r.loppu}&lang=${r.lang}")))
    writeExcel(raportitService.lukio2019KoulutuksenOpintopistekertyma(r, t), t)
  }

  get("/lukiodiaibinternationalopiskelijamaarat") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.lukiokoulutus)
    val r = parseRaporttiPäivältäRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, r.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=lukiodiaibinternationalopiskelijamaarat&oppilaitosOid=${r.oppilaitosOid}&paiva=${r.paiva}&lang=${r.lang}")))
    writeExcel(raportitService.lukioDiaIbInternationalOpiskelijaMaaratRaportti(r, t), t)
  }

  get("/luvaopiskelijamaarat") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.luva)
    val r = parseRaporttiPäivältäRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, r.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=luvaopiskelijamaarat&oppilaitosOid=${r.oppilaitosOid}&paiva=${r.paiva}&lang=${r.lang}")))
    writeExcel(raportitService.lukioonValmistavanKoulutuksenOpiskelijaMaaratRaportti(r, t), t)
  }

  get("/aikuisten-perusopetus-suoritustietojen-tarkistus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.aikuistenperusopetus)
    val parsedRequest = parseAikuistenPerusopetusRaporttiRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=aikuistenperusopetuksensuoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.aikuistenPerusopetus(parsedRequest, t), t)
  }


  get("/muuammatillinen") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=muuammatillinen&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.muuAmmatillinen(parsedRequest, t), t)
  }

  get("/topksammatillinen") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
    val parsedRequest = parseAikajaksoRaporttiRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=topksammatillinen&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.topksAmmatillinen(parsedRequest, t), t)
  }

  get("/esiopetus") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.esiopetus)
    val date = getLocalDateParam("paiva")
    val password = getStringParam("password")
    val token = params.get("downloadToken")
    val lang = getStringParam("lang")
    val t = new LocalizationReader(application.koskiLocalizationRepository, lang)

    val resp = getStringParam("oppilaitosOid") match {
      case organisaatioService.ostopalveluRootOid =>
        esiopetusService.buildOstopalveluRaportti(date, password, token, t)
      case oid =>
        esiopetusService.buildOrganisaatioRaportti(validateOrganisaatioOid(oid), date, password, token, t)
    }

    writeExcel(resp, t)
  }

  get("/esiopetuksenoppijamaaratraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.esiopetus)
    val parsedRequest = parseRaporttiPäivältäRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=esiopetuksenoppijamaaratraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.esiopetuksenOppijamäärät(parsedRequest, t), t)
  }

  get("/aikuistenperusopetuksenoppijamaaratraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.aikuistenperusopetus)
    val parsedRequest = parseRaporttiPäivältäRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=aikuistenperusopetuksenoppijamaaratraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.aikuistenperusopetuksenOppijamäärät(parsedRequest, t), t)
  }

  get("/aikuistenperusopetuksenkurssikertymaraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.aikuistenperusopetus)
    val parsedRequest = parseAikajaksoRaporttiRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=aikuistenperusopetuksenkurssikertymaraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.aikuistenperusopetuksenKurssikertymä(parsedRequest, t), t)
  }

  get("/perusopetuksenoppijamaaratraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.perusopetus)
    val parsedRequest = parseRaporttiPäivältäRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=perusopetuksenoppijamaaratraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.perusopetuksenOppijamäärät(parsedRequest, t), t)
  }

  get("/perusopetuksenlisaopetuksenoppijamaaratraportti") {
    requireOpiskeluoikeudenKayttooikeudet(OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus)
    val parsedRequest = parseRaporttiPäivältäRequest
    val t = new LocalizationReader(application.koskiLocalizationRepository, parsedRequest.lang)
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_RAPORTTI, session, Map(hakuEhto -> s"raportti=perusopetuksenlisaopetuksenoppijamaaratraportti&oppilaitosOid=${parsedRequest.oppilaitosOid}&paiva=${parsedRequest.paiva}&lang=${parsedRequest.lang}")))
    writeExcel(raportitService.perusopetuksenLisäopetuksenOppijamäärät(parsedRequest, t), t)
  }

  private def requireOpiskeluoikeudenKayttooikeudet(opiskeluoikeudenTyyppiViite: Koodistokoodiviite) = {
    if (!session.allowedOpiskeluoikeusTyypit.contains(opiskeluoikeudenTyyppiViite.koodiarvo)) {
      haltWithStatus(KoskiErrorCategory.forbidden.opiskeluoikeudenTyyppi())
    }
  }

  private def writeExcel(raportti: OppilaitosRaporttiResponse, t: LocalizationReader) = {
    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    response.setHeader("Content-Disposition", s"""attachment; filename="${raportti.filename}"""")
    raportti.downloadToken.foreach { t => response.addCookie(Cookie("koskiDownloadToken", t)(CookieOptions(path = "/", maxAge = 600))) }
    ExcelWriter.writeExcel(
      raportti.workbookSettings,
      raportti.sheets,
      ExcelWriter.BooleanCellStyleLocalizedValues(t),
      response.getOutputStream
    )
  }

  private def parseAikajaksoRaporttiRequest: AikajaksoRaporttiRequest = {
    val oppilaitosOid = getOppilaitosOid
    val (alku, loppu) = getAlkuLoppuParams
    val password = getStringParam("password")
    val downloadToken = params.get("downloadToken")
    val lang = getStringParam("lang")

    AikajaksoRaporttiRequest(oppilaitosOid, downloadToken, password, alku, loppu, lang)
  }

  private def parseRaporttiPäivältäRequest: RaporttiPäivältäRequest = {
    val paiva = getLocalDateParam("paiva")
    val password = getStringParam("password")
    val downloadToken = params.get("downloadToken")
    val oppilaitosOid = getOppilaitosOid
    val lang = getStringParam("lang")

    RaporttiPäivältäRequest(oppilaitosOid, downloadToken, password, paiva, lang)
  }

  private def parseAikajaksoRaporttiAikarajauksellaRequest: AikajaksoRaporttiAikarajauksellaRequest = {
    val oppilaitosOid = getOppilaitosOid
    val (alku, loppu) = getAlkuLoppuParams
    val password = getStringParam("password")
    val downloadToken = params.get("downloadToken")
    val osasuoritustenAikarajaus = getOptionalBooleanParam("osasuoritustenAikarajaus").getOrElse(false)
    val lang = getStringParam("lang")

    AikajaksoRaporttiAikarajauksellaRequest(oppilaitosOid, downloadToken, password, alku, loppu, osasuoritustenAikarajaus, lang)
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
      osasuoritustenAikarajaus = getOptionalBooleanParam("osasuoritustenAikarajaus").getOrElse(false),
      raportinTyyppi = raportinTyyppi,
      lang = getStringParam("lang")
    )
  }

  private def parseVuosiluokkaRequest: PerusopetuksenVuosiluokkaRequest = {
    PerusopetuksenVuosiluokkaRequest(
      oppilaitosOid = getOppilaitosOid,
      downloadToken = params.get("downloadToken"),
      password = getStringParam("password"),
      paiva = getLocalDateParam("paiva"),
      vuosiluokka = getStringParam("vuosiluokka"),
      lang = getStringParam("lang")
    )
  }

  private def getOppilaitosOid: Organisaatio.Oid = {
    validateOrganisaatioOid(getStringParam("oppilaitosOid"))
  }

  private def validateOrganisaatioOid(oppilaitosOid: String) =
    OrganisaatioOid.validateOrganisaatioOid(oppilaitosOid) match {
      case Left(error) => haltWithStatus(error)
      case Right(oid) if !session.hasRaporttiReadAccess(oid) => haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
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
}

