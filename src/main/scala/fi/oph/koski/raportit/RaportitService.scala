package fi.oph.koski.raportit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.OrganisaatioHierarkia
import fi.oph.koski.raportit.aikuistenperusopetus._
import fi.oph.koski.raportit.esiopetus.{EsiopetuksenOppijamäärätAikajaksovirheetRaportti, EsiopetuksenOppijamäärätRaportti}
import fi.oph.koski.raportit.lukio.LukioOppiaineenOppimaaranKurssikertymat.{AikuistenOppimäärä, NuortenOppimäärä}
import fi.oph.koski.raportit.lukio._
import fi.oph.koski.raportit.lukio.lops2021._
import fi.oph.koski.raportit.perusopetus.{PerusopetuksenOppijamäärätAikajaksovirheetRaportti, PerusopetuksenOppijamäärätRaportti, PerusopetuksenRaportitRepository, PerusopetuksenVuosiluokkaRaportti}
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid

import java.time.LocalDateTime

class RaportitService(application: KoskiApplication) {
  private val raportointiDatabase = application.raportointiDatabase
  private val perusopetusRepository = PerusopetuksenRaportitRepository(raportointiDatabase.db)
  private val accessResolver = RaportitAccessResolver(application)
  private lazy val organisaatioService = application.organisaatioService
  private val lukioRepository = LukioRaportitRepository(raportointiDatabase.db)
  private val lukio2019Repository = Lukio2019RaportitRepository(raportointiDatabase.db)
  private val lukioDiaIbInternationalOpiskelijaMaaratRaportti = LukioDiaIbInternationalOpiskelijamaaratRaportti(raportointiDatabase.db)
  private val ammatillisenRaportitRepository = AmmatillisenRaportitRepository(raportointiDatabase.db)
  private val aikuistenPerusopetusRepository = AikuistenPerusopetusRaporttiRepository(raportointiDatabase.db)
  private val muuammatillinenRaportti = MuuAmmatillinenRaporttiBuilder(raportointiDatabase.db)
  private val topksAmmatillinenRaportti = TOPKSAmmatillinenRaporttiBuilder(raportointiDatabase.db)
  private val esiopetuksenOppijamäärätRaportti = EsiopetuksenOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)
  private val esiopetuksenOppijamäärätAikajaksovirheetRaportti = EsiopetuksenOppijamäärätAikajaksovirheetRaportti(raportointiDatabase.db, application.organisaatioService)
  private val aikuistenPerusopetuksenOppijamäärätRaportti = AikuistenPerusopetuksenOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)
  private val aikuistenPerusopetuksenOppimääränKurssikertymätRaportti = AikuistenPerusopetuksenOppimääränKurssikertymät(raportointiDatabase.db)
  private val aikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRaportti = AikuistenPerusopetuksenAineopiskelijoidenKurssikertymät(raportointiDatabase.db)
  private val aikuistenPerusopetuksenMuutaKauttaRahoitetutKurssitRaportti = AikuistenPerusopetuksenMuutaKauttaRahoitetutKurssit(raportointiDatabase.db)
  private val aikuistenPerusopetuksenEiRahoitustietoaKurssitRaportti = AikuistenPerusopetuksenEiRahoitustietoaKurssit(raportointiDatabase.db)
  private val aikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetRaportti = AikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetKurssit(raportointiDatabase.db)
  private val aikuistenPerusopetuksenEriVuonnaKorotetutKurssitRaportti = AikuistenPerusopetuksenEriVuonnaKorotetutKurssit(raportointiDatabase.db)
  private val perusopetuksenOppijamäärätRaportti = PerusopetuksenOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)
  private val perusopetuksenOppijamäärätAikajaksovirheetRaportti = PerusopetuksenOppijamäärätAikajaksovirheetRaportti(raportointiDatabase.db, application.organisaatioService)
  private val perusopetuksenLisäopetuksenOppijamäärätRaportti = PerusopetuksenLisäopetusOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)
  private val ibSuoritustiedotRepository = IBSuoritustiedotRaporttiRepository(raportointiDatabase.db)
  private val perusopetukseenValmistavanRepository = PerusopetukseenValmistavanRaportitRepository(raportointiDatabase.db)

  def viimeisinOpiskeluoikeuspäivitystenVastaanottoaika: LocalDateTime = {
    val status = raportointiDatabase.status
    status
      .statuses
      .find(_.name == "opiskeluoikeudet")
      .flatMap(_.dueTime)
      .getOrElse(status.startedTime.get)
      .toLocalDateTime
  }

  def paallekkaisetOpiskeluoikeudet(
    request: AikajaksoRaporttiRequest,
    t: LocalizationReader
  ): OppilaitosRaporttiResponse = {
    val oidit = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid).toSeq
    OppilaitosRaporttiResponse(
      sheets = Seq(
        PaallekkaisetOpiskeluoikeudet.datasheet(oidit, request.alku, request.loppu, raportointiDatabase)(t)
      ),
      workbookSettings = WorkbookSettings("", Some(request.password)),
      filename = s"${t.get("raportti-excel-paallekkaiset-opiskeluoikeudet-tiedoston-etuliite")}_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def opiskelijaVuositiedot(request: AikajaksoRaporttiRequest, t: LocalizationReader): OppilaitosRaporttiResponse = {
    aikajaksoRaportti(request, AmmatillinenOpiskalijavuositiedotRaportti, t)
  }

  def ammatillinenTutkintoSuoritustietojenTarkistus(
    request: AikajaksoRaporttiAikarajauksellaRequest,
    t: LocalizationReader
  ): OppilaitosRaporttiResponse = {
    OppilaitosRaporttiResponse(
      sheets = Seq(
        DataSheet(
          title = t.get("raportti-excel-opiskeluoikeudet-sheet-name"),
          rows = AmmatillinenTutkintoRaportti.buildRaportti(request, ammatillisenRaportitRepository, t),
          columnSettings = AmmatillinenTutkintoRaportti.columnSettings(t)
        ),
        DocumentationSheet(
          title = t.get("raportti-excel-ohjeet-sheet-name"),
          text = AmmatillinenTutkintoRaportti.documentation(request, raportointiDatabase.status.startedTime.get.toLocalDateTime, t))
      ),
      workbookSettings = WorkbookSettings(AmmatillinenTutkintoRaportti.title(request, t), Some(request.password)),
      filename = AmmatillinenTutkintoRaportti.filename(request, t),
      downloadToken = request.downloadToken
    )
  }

  def ammatillinenOsittainenSuoritustietojenTarkistus(request: AikajaksoRaporttiAikarajauksellaRequest, t: LocalizationReader): OppilaitosRaporttiResponse = {
    OppilaitosRaporttiResponse(
      sheets = Seq(
        DataSheet(
          title = t.get("raportti-excel-opiskeluoikeudet-sheet-name"),
          rows = AmmatillinenOsittainenRaportti.buildRaportti(request, ammatillisenRaportitRepository, t),
          columnSettings = AmmatillinenOsittainenRaportti.columnSettings(t)
        ),
        DocumentationSheet(
          title = t.get("raportti-excel-ohjeet-sheet-name"),
          text = AmmatillinenOsittainenRaportti.documentation(request, raportointiDatabase.status.startedTime.get.toLocalDateTime, t)
        )
      ),
      workbookSettings = WorkbookSettings(AmmatillinenOsittainenRaportti.title(request, t), Some(request.password)),
      filename = AmmatillinenOsittainenRaportti.filename(request, t),
      downloadToken = request.downloadToken
    )
  }

  def perusopetuksenVuosiluokka(request: PerusopetuksenVuosiluokkaRequest, t: LocalizationReader): OppilaitosRaporttiResponse = {
    perusopetuksenVuosiluokka(request, PerusopetuksenVuosiluokkaRaportti, t)
  }

  def valmistavanopetuksenraportti(request: AikajaksoRaporttiAikarajauksellaRequest, t: LocalizationReader) = {
    val valmistavanRaportti = PerusopetukseenValmistavanRaportti(perusopetukseenValmistavanRepository, t)
    val raportti = valmistavanRaportti
      .buildRows(Seq(request.oppilaitosOid), request.alku, request.loppu, t)

    OppilaitosRaporttiResponse(
      sheets = Seq(valmistavanRaportti.buildDataSheet(raportti)),
      workbookSettings = WorkbookSettings(
        s"${t.get("raportti-excel-valmistava-opetus-title")}_${request.oppilaitosOid}", Some(request.password)
      ),
      filename = s"${t.get("valmistavaopetus_koski_raportti")}_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def lukioraportti(request: AikajaksoRaporttiAikarajauksellaRequest, t: LocalizationReader) = {
    OppilaitosRaporttiResponse(
      sheets = LukioRaportti(lukioRepository, t)
        .buildRaportti(request.oppilaitosOid, request.alku, request.loppu, request.osasuoritustenAikarajaus),
      workbookSettings = WorkbookSettings(
        s"${t.get("raportti-excel-lukio-opiskeluoikeus-title")}_${request.oppilaitosOid}", Some(request.password)
      ),
      filename = s"${t.get("raportti-excel-lukio-opiskeluoikeus-tiedoston-etuliite")}_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def lukioraportti2019(request: AikajaksoRaporttiAikarajauksellaRequest, t: LocalizationReader) = {
    OppilaitosRaporttiResponse(
      sheets = Lukio2019Raportti(lukio2019Repository, t)
        .buildRaportti(request.oppilaitosOid, request.alku, request.loppu, request.osasuoritustenAikarajaus),
      workbookSettings = WorkbookSettings(
        s"${t.get("raportti-excel-lukio-opiskeluoikeus-title")}_${request.oppilaitosOid}", Some(request.password)
      ),
      filename = s"${t.get("raportti-excel-lukio2019-opiskeluoikeus-tiedoston-etuliite")}_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def lukioDiaIbInternationalOpiskelijaMaaratRaportti(
    request: RaporttiPäivältäRequest,
    t: LocalizationReader
  ): OppilaitosRaporttiResponse = {
    OppilaitosRaporttiResponse(
      sheets = Seq(lukioDiaIbInternationalOpiskelijaMaaratRaportti
        .build(accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid).toList, request.paiva, t)
      ),
      workbookSettings = WorkbookSettings("", Some(request.password)),
      filename = s"${t.get("raportti-excel-lukio-opiskelijamäärät-tiedoston-etuliite")}_${request.paiva.toString.replaceAll("-", "")}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def lukioKoulutuksenKurssikertyma(
    request: AikajaksoRaporttiRequest,
    t: LocalizationReader
  ): OppilaitosRaporttiResponse = {
    val oppilaitosOidit = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid).toList
    OppilaitosRaporttiResponse(
      sheets = Seq(
        LukioOppimaaranKussikertymat.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t),
        LukioOppiaineenOppimaaranKurssikertymat.datasheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t, NuortenOppimäärä),
        LukioOppiaineenOppimaaranKurssikertymat.datasheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t, AikuistenOppimäärä),
        LukioMuutaKauttaRahoitetut.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t),
        LukioRahoitusmuotoEiTiedossa.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t),
        LukioOppiaineOpiskeluoikeudenUlkopuoliset.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t),
        LukioOppiaineEriVuonnaKorotetutKurssit.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t)
      ),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-lukio-kurssikertymät-title"), Some(request.password)),
      filename = s"${t.get("raportti-excel-lukio-kurssikertymät-tiedoston-etuliite")}_${request.alku.toString.replaceAll("-", "")}-${request.loppu.toString.replaceAll("-", "")}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def lukio2019KoulutuksenOpintopistekertyma(
    request: AikajaksoRaporttiRequest,
    t: LocalizationReader
  ): OppilaitosRaporttiResponse = {
    val oppilaitosOidit = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid).toList
    OppilaitosRaporttiResponse(
      sheets = Seq(
        Lukio2019OppimaaranOpintopistekertymat.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t),
        Lukio2019AineopintojenOpintopistekertymat.datasheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t, NuortenOppimäärä),
        Lukio2019AineopintojenOpintopistekertymat.datasheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t, AikuistenOppimäärä),
        Lukio2019MuutaKauttaRahoitetut.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t),
        Lukio2019RahoitusmuotoEiTiedossa.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t),
        Lukio2019OppiaineOpiskeluoikeudenUlkopuoliset.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t),
        Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase, t)
      ),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-lukio2019-opintopistekertymät-title"), Some(request.password)),
      filename = s"${t.get("raportti-excel-lukio2019-opintopistekertymät-tiedoston-etuliite")}_${request.alku.toString.replaceAll("-", "")}-${request.loppu.toString.replaceAll("-", "")}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def lukioonValmistavanKoulutuksenOpiskelijaMaaratRaportti(
    request: RaporttiPäivältäRequest,
    t: LocalizationReader
  ): OppilaitosRaporttiResponse = {
    val oidit = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid).toList
    OppilaitosRaporttiResponse(
      sheets = Seq(
        LukioonValmistavanKoulutuksenOpiskelijamaaratRaportti.dataSheet(oidit, request.paiva, raportointiDatabase, t)
      ),
      workbookSettings = WorkbookSettings("", Some(request.password)),
      filename = s"${t.get("raportti-excel-luva-opiskelijamäärät-tiedoston-etuliite")}_${request.paiva.toString.replaceAll("-", "")}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def aikuistenPerusopetus(request: AikuistenPerusopetusRaporttiRequest, t: LocalizationReader) = {
    OppilaitosRaporttiResponse(
      sheets = AikuistenPerusopetusRaportti(
        aikuistenPerusopetusRepository,
        request.raportinTyyppi,
        request.oppilaitosOid,
        request.alku,
        request.loppu,
        request.osasuoritustenAikarajaus,
        t
      ).build(),
      workbookSettings = WorkbookSettings(s"${t.get("raportti-excel-aikuistenperusopetus-title-etuliite")}_${request.oppilaitosOid}", Some(request.password)),
      filename = s"${t.get("raportti-excel-aikuistenperusopetus-tiedoston-etuliite")}_${AikuistenPerusopetusRaportti.raporttiTypeLokalisoitu(request.raportinTyyppi.typeName, t)}_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def muuAmmatillinen(request: AikajaksoRaporttiRequest, t: LocalizationReader) = OppilaitosRaporttiResponse(
    sheets = Seq(muuammatillinenRaportti.build(request.oppilaitosOid, request.alku, request.loppu, t)),
    workbookSettings = WorkbookSettings(t.get("raportti-excel-muu-ammatillinen-title"), Some(request.password)),
    filename = s"${t.get("raportti-excel-muu-ammatillinen-tiedoston-etuliite")}_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-","")}-${request.loppu.toString.replaceAll("-","")}.xlsx",
    downloadToken = request.downloadToken
  )

  def topksAmmatillinen(request: AikajaksoRaporttiRequest, t: LocalizationReader) = OppilaitosRaporttiResponse(
    sheets = Seq(topksAmmatillinenRaportti.build(request.oppilaitosOid, request.alku, request.loppu, t)),
    workbookSettings = WorkbookSettings(t.get("raportti-excel-ammatillinen-topks-title"), Some(request.password)),
    filename = s"${t.get("raportti-excel-ammatillinen-topks-tiedoston-etuliite")}_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-","")}-${request.loppu.toString.replaceAll("-","")}.xlsx",
    downloadToken = request.downloadToken
  )

  def esiopetuksenOppijamäärät(request: RaporttiPäivältäRequest, t: LocalizationReader)(implicit u: KoskiSpecificSession) = {

    val oppilaitosOids = request.oppilaitosOid match {
      case application.organisaatioService.ostopalveluRootOid =>
        application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)
      case oid => List(oid)
    }

    OppilaitosRaporttiResponse(
      sheets = Seq(
        esiopetuksenOppijamäärätRaportti.build(oppilaitosOids, request.paiva, t),
        esiopetuksenOppijamäärätAikajaksovirheetRaportti.build(oppilaitosOids, request.paiva, t),
        DocumentationSheet(t.get("raportti-excel-ohjeet-sheet-name"), t.get("raportti-excel-aikajaksovirheet-ohje-body"))
      ),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-esiopetus-vos-title"), Some(request.password)),
      filename = s"${t.get("raportti-excel-esiopetus-vos-tiedoston-etuliite")}-${request.paiva}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def aikuistenperusopetuksenKurssikertymä(request: AikajaksoRaporttiRequest, t: LocalizationReader)(implicit u: KoskiSpecificSession) = {
    val oppilaitosOids = validateOids(List(request.oppilaitosOid))

    OppilaitosRaporttiResponse(
      sheets = Seq(
        aikuistenPerusopetuksenOppimääränKurssikertymätRaportti.build(oppilaitosOids, request.alku, request.loppu, t),
        aikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRaportti.build(oppilaitosOids, request.alku, request.loppu, t),
        aikuistenPerusopetuksenMuutaKauttaRahoitetutKurssitRaportti.build(oppilaitosOids, request.alku, request.loppu, t),
        aikuistenPerusopetuksenEiRahoitustietoaKurssitRaportti.build(oppilaitosOids, request.alku, request.loppu, t),
        aikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetRaportti.build(oppilaitosOids, request.alku, request.loppu, t),
        aikuistenPerusopetuksenEriVuonnaKorotetutKurssitRaportti.build(oppilaitosOids, request.alku, request.loppu, t),
      ),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-aikuistenperusopetus-kurssikertymä-title-etuliite"), Some(request.password)),
      filename = s"${t.get("raportti-excel-aikuistenperusopetus-kurssikertymä-tiedoston-etuliite")}-${request.alku.toString.replaceAll("-", "")}-${request.loppu.toString.replaceAll("-", "")}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def aikuistenperusopetuksenOppijamäärät(request: RaporttiPäivältäRequest, t: LocalizationReader)(implicit u: KoskiSpecificSession) = {
    val oppilaitosOids = request.oppilaitosOid match {
      case application.organisaatioService.ostopalveluRootOid =>
        application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)
      case oid =>
        application.organisaatioService.organisaationAlaisetOrganisaatiot(oid)
    }
    OppilaitosRaporttiResponse(
      sheets = Seq(aikuistenPerusopetuksenOppijamäärätRaportti.build(oppilaitosOids, request.paiva, t)),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-aikuistenperusopetus-vos-title-etuliite"), Some(request.password)),
      filename = s"${t.get("raportti-excel-aikuistenperusopetus-vos-tiedoston-etuliite")}-${request.paiva}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def perusopetuksenOppijamäärät(request: RaporttiPäivältäRequest, t: LocalizationReader)(implicit u: KoskiSpecificSession) = {
    val oppilaitosOids = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid, "perusopetus")
    OppilaitosRaporttiResponse(
      sheets = Seq(
        perusopetuksenOppijamäärätRaportti.build(oppilaitosOids, request.paiva, t),
        perusopetuksenOppijamäärätAikajaksovirheetRaportti.build(oppilaitosOids, request.paiva, t),
        DocumentationSheet(t.get("raportti-excel-ohjeet-sheet-name"), t.get("raportti-excel-aikajaksovirheet-ohje-body"))
      ),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-perusopetus-vos-title"), Some(request.password)),
      filename = s"${t.get("raportti-excel-perusopetus-vos-tiedoston-etuliite")}-${request.paiva}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def perusopetuksenLisäopetuksenOppijamäärät(request: RaporttiPäivältäRequest, t: LocalizationReader)(implicit u: KoskiSpecificSession) = {
    val oppilaitosOids = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid)
    OppilaitosRaporttiResponse(
      sheets = Seq(perusopetuksenLisäopetuksenOppijamäärätRaportti.build(oppilaitosOids.toSeq, request.paiva, t)),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-perusopetuksenlisaopetus-vos-title"), Some(request.password)),
      filename = s"${t.get("raportti-excel-perusopetuksenlisaopetus-vos-tiedoston-etuliite")}-${request.paiva}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def ibSuoritustiedot(request: IBSuoritustiedotRaporttiRequest, t: LocalizationReader)
    (implicit u: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    OppilaitosRaporttiResponse(
      sheets = IBSuoritustiedotRaportti(ibSuoritustiedotRepository, t)
        .build(
          request.oppilaitosOid,
          request.alku,
          request.loppu,
          request.osasuoritustenAikarajaus,
          request.raportinTyyppi
        ),
      workbookSettings = WorkbookSettings(t.get("raportti-excel-ib-suoritustiedot-title"), Some(request.password)),
      filename = s"${t.get("raportti-excel-ib-suoritustiedot-tiedoston-etuliite")}_${IBSuoritustiedotRaporttiType.raporttiTypeLokalisoitu(request.raportinTyyppi, t)}_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def getRaportinOrganisatiotJaRaporttiTyypit(organisaatiot: Iterable[OrganisaatioHierarkia])(implicit user: KoskiSpecificSession) = {
    val koulutusmuodot = getKoulutusmuodot()
    buildOrganisaatioJaRaporttiTyypit(organisaatiot, koulutusmuodot)
  }

  def getKoulutusmuodot() = {
    val query = sql"""
      SELECT
        oppilaitos_oid,
        array_agg(DISTINCT koulutusmuoto) as koulutusmuodot
      FROM r_opiskeluoikeus
      GROUP BY oppilaitos_oid
    """
    raportointiDatabase.runDbSync(query.as[(String, Seq[String])]).toMap
  }

  private def aikajaksoRaportti(request: AikajaksoRaporttiRequest, raporttiBuilder: AikajaksoRaportti, t: LocalizationReader) = {
    val rows = raporttiBuilder.buildRaportti(raportointiDatabase, request.oppilaitosOid, request.alku, request.loppu, t)
    val documentation = DocumentationSheet(
      title = t.get("raportti-excel-ohjeet-sheet-name"),
      text = raporttiBuilder.documentation(
        request.oppilaitosOid,
        request.alku,
        request.loppu,
        raportointiDatabase.status.startedTime.get.toLocalDateTime,
        t
      )
    )
    val data = DataSheet(t.get("raportti-excel-opiskeluoikeudet-sheet-name"), rows, raporttiBuilder.columnSettings(t))

    OppilaitosRaporttiResponse(
      sheets = Seq(data, documentation),
      workbookSettings = WorkbookSettings(raporttiBuilder.title(request.oppilaitosOid, request.alku, request.loppu, t), Some(request.password)),
      filename = raporttiBuilder.filename(request.oppilaitosOid, request.alku, request.loppu, t),
      downloadToken = request.downloadToken
    )
  }

  private def perusopetuksenVuosiluokka(request: PerusopetuksenVuosiluokkaRequest, raporttiBuilder: VuosiluokkaRaporttiPaivalta, t: LocalizationReader) = {
    val oppilaitosOids = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid, "perusopetus")
    val rows = raporttiBuilder.buildRaportti(perusopetusRepository, oppilaitosOids, request.paiva, request.vuosiluokka, t)
    val documentation = DocumentationSheet(t.get("raportti-excel-ohjeet-sheet-name"), raporttiBuilder.documentation(t))
    val data = DataSheet(t.get("raportti-excel-opiskeluoikeudet-sheet-name"), rows, raporttiBuilder.columnSettings(t))

    OppilaitosRaporttiResponse(
      sheets = Seq(data, documentation),
      workbookSettings = WorkbookSettings(raporttiBuilder.title(t.get("raportti-excel-perusopetus-title-etuliite"), request.oppilaitosOid, request.paiva, request.vuosiluokka), Some(request.password)),
      filename = raporttiBuilder.filename(t.get("raportti-excel-perusopetus-tiedoston-etuliite"), request.oppilaitosOid, request.paiva, request.vuosiluokka),
      downloadToken = request.downloadToken
    )
  }

  private def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }

  private def buildOrganisaatioJaRaporttiTyypit(organisaatiot: Iterable[OrganisaatioHierarkia], koulutusmuodot: Map[String, Seq[String]])(implicit user: KoskiSpecificSession): List[OrganisaatioJaRaporttiTyypit] =
    organisaatiot
      .filter(_.organisaatiotyypit.intersect(raporttinakymanOrganisaatiotyypit).nonEmpty)
      .map(_.sortBy(user.lang))
      .map((organisaatio) => {
        OrganisaatioJaRaporttiTyypit(
          nimi = organisaatio.nimi,
          oid = organisaatio.oid,
          organisaatiotyypit = organisaatio.organisaatiotyypit,
          raportit = organisaatio.oid match {
            case organisaatioService.ostopalveluRootOid => Set(EsiopetuksenRaportti.toString, EsiopetuksenOppijaMäärienRaportti.toString)
            case _: String => accessResolver.mahdollisetRaporttienTyypitOrganisaatiolle(organisaatio, koulutusmuodot).map(_.toString)
          },
          children = organisaatio.oid match {
            case organisaatioService.ostopalveluRootOid => Nil
            case _ => buildOrganisaatioJaRaporttiTyypit(organisaatio.children, koulutusmuodot)
          }

        )
      })
      .toList
      .sortBy(_.nimi.get(user.lang))

  private val raporttinakymanOrganisaatiotyypit = List(
    "OPPILAITOS",
    "OPPISOPIMUSTOIMIPISTE",
    "KOULUTUSTOIMIJA",
    "VARHAISKASVATUKSEN_TOIMIPAIKKA",
    "OSTOPALVELUTAIPALVELUSETELI"
  )
}

case class DynamicResponse(sheets: Seq[Sheet], workbookSettings: WorkbookSettings, filename: String, downloadToken: Option[String])

case class OrganisaatioJaRaporttiTyypit(
  nimi: LocalizedString,
  oid: String,
  organisaatiotyypit: List[String],
  raportit: Set[String],
  children: List[OrganisaatioJaRaporttiTyypit]
)
