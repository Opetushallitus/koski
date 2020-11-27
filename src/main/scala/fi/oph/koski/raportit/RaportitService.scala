package fi.oph.koski.raportit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.raportit.aikuistenperusopetus.{AikuistenPerusopetuksenAineopiskelijoidenKurssikertymät, AikuistenPerusopetuksenEiRahoitustietoaKurssit, AikuistenPerusopetuksenMuutaKauttaRahoitetutKurssit, AikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetKurssit, AikuistenPerusopetuksenOppijamäärätRaportti, AikuistenPerusopetuksenOppimääränKurssikertymät, AikuistenPerusopetusRaportti, AikuistenPerusopetusRaporttiRepository}
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid

class RaportitService(application: KoskiApplication) {
  private val raportointiDatabase = application.raportointiDatabase
  private val perusopetusRepository = PerusopetuksenRaportitRepository(raportointiDatabase.db)
  private val accessResolver = RaportitAccessResolver(application)
  private val lukioRepository = LukioRaportitRepository(raportointiDatabase.db)
  private val lukioDiaIbInternationalOpiskelijaMaaratRaportti = LukioDiaIbInternationalOpiskelijamaaratRaportti(raportointiDatabase.db)
  private val ammatillisenRaportitRepository = AmmatillisenRaportitRepository(raportointiDatabase.db)
  private val aikuistenPerusopetusRepository = AikuistenPerusopetusRaporttiRepository(raportointiDatabase.db)
  private val muuammatillinenRaportti = MuuAmmatillinenRaporttiBuilder(raportointiDatabase.db)
  private val topksAmmatillinenRaportti = TOPKSAmmatillinenRaporttiBuilder(raportointiDatabase.db)
  private val esiopetuksenOppijamäärätRaportti = EsiopetuksenOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)
  private val aikuistenPerusopetuksenOppijamäärätRaportti = AikuistenPerusopetuksenOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)
  private val aikuistenPerusopetuksenOppimääränKurssikertymätRaportti = AikuistenPerusopetuksenOppimääränKurssikertymät(raportointiDatabase.db)
  private val aikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRaportti = AikuistenPerusopetuksenAineopiskelijoidenKurssikertymät(raportointiDatabase.db)
  private val aikuistenPerusopetuksenMuutaKauttaRahoitetutKurssitRaportti = AikuistenPerusopetuksenMuutaKauttaRahoitetutKurssit(raportointiDatabase.db)
  private val aikuistenPerusopetuksenEiRahoitustietoaKurssitRaportti = AikuistenPerusopetuksenEiRahoitustietoaKurssit(raportointiDatabase.db)
  private val aikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetRaportti = AikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetKurssit(raportointiDatabase.db)
  private val perusopetuksenOppijamäärätRaportti = PerusopetuksenOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)
  private val perusopetuksenLisäopetuksenOppijamäärätRaportti = PerusopetuksenLisäopetusOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)

  def paallekkaisetOpiskeluoikeudet(request: AikajaksoRaporttiRequest): OppilaitosRaporttiResponse = {
    val oidit = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid).toSeq
    OppilaitosRaporttiResponse(
      sheets = Seq(
        PaallekkaisetOpiskeluoikeudet.datasheet(oidit, request.alku, request.loppu, raportointiDatabase)
      ),
      workbookSettings = WorkbookSettings("", Some(request.password)),
      filename = s"paallekkaiset_opiskeluoikeudet_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def opiskelijaVuositiedot(request: AikajaksoRaporttiRequest): OppilaitosRaporttiResponse = {
    aikajaksoRaportti(request, AmmatillinenOpiskalijavuositiedotRaportti)
  }

  def ammatillinenTutkintoSuoritustietojenTarkistus(request: AikajaksoRaporttiAikarajauksellaRequest): OppilaitosRaporttiResponse = {
    OppilaitosRaporttiResponse(
      sheets = Seq(
        DataSheet("Opiskeluoikeudet", AmmatillinenTutkintoRaportti.buildRaportti(request, ammatillisenRaportitRepository), AmmatillinenTutkintoRaportti.columnSettings),
        DocumentationSheet("Ohjeet", AmmatillinenTutkintoRaportti.documentation(request, raportointiDatabase.status.completionTime.get.toLocalDateTime))
      ),
      workbookSettings = WorkbookSettings(AmmatillinenTutkintoRaportti.title(request), Some(request.password)),
      filename = AmmatillinenTutkintoRaportti.filename(request),
      downloadToken = request.downloadToken
    )
  }

  def ammatillinenOsittainenSuoritustietojenTarkistus(request: AikajaksoRaporttiAikarajauksellaRequest): OppilaitosRaporttiResponse = {
    OppilaitosRaporttiResponse(
      sheets = Seq(
        DataSheet("Opiskeluoikeudet", AmmatillinenOsittainenRaportti.buildRaportti(request, ammatillisenRaportitRepository), AmmatillinenOsittainenRaportti.columnSettings),
        DocumentationSheet("Ohjeet", AmmatillinenOsittainenRaportti.documentation(request, raportointiDatabase.status.completionTime.get.toLocalDateTime))
      ),
      workbookSettings = WorkbookSettings(AmmatillinenOsittainenRaportti.title(request), Some(request.password)),
      filename = AmmatillinenOsittainenRaportti.filename(request),
      downloadToken = request.downloadToken
    )
  }

  def perusopetuksenVuosiluokka(request: PerusopetuksenVuosiluokkaRequest): OppilaitosRaporttiResponse = {
    perusopetuksenVuosiluokka(request, PerusopetuksenVuosiluokkaRaportti)
  }

  def lukioraportti(request: AikajaksoRaporttiAikarajauksellaRequest) = {
    OppilaitosRaporttiResponse(
      sheets = LukioRaportti(lukioRepository).buildRaportti(request.oppilaitosOid, request.alku, request.loppu, request.osasuoritustenAikarajaus),
      workbookSettings = WorkbookSettings(s"Suoritustietojen_tarkistus_${request.oppilaitosOid}", Some(request.password)),
      filename = s"lukio_suoritustietojentarkistus_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def lukioDiaIbInternationalOpiskelijaMaaratRaportti(request: RaporttiPäivältäRequest): OppilaitosRaporttiResponse = {
    OppilaitosRaporttiResponse(
      sheets = Seq(lukioDiaIbInternationalOpiskelijaMaaratRaportti.build(accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid).toList, request.paiva)),
      workbookSettings = WorkbookSettings("", Some(request.password)),
      filename = s"lukiokoulutus_opiskelijamaarat_${request.paiva.toString.replaceAll("-", "")}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def lukioKoulutuksenKurssikertyma(request: AikajaksoRaporttiRequest): OppilaitosRaporttiResponse = {
    val oppilaitosOidit = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid).toList
    OppilaitosRaporttiResponse(
      sheets = Seq(
        LukioOppimaaranKussikertymat.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase),
        LukioOppiaineenOppimaaranKurssikertymat.datasheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase),
        LukioMuutaKauttaRahoitetut.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase),
        LukioRahoitusmuotoEiTiedossa.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase),
        LukioOppiaineOpiskeluoikeudenUlkopuoliset.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase)
      ),
      workbookSettings = WorkbookSettings("Kurssikertymat", Some(request.password)),
      filename = s"lukion_kurssikertymat_${request.alku.toString.replaceAll("-", "")}-${request.loppu.toString.replaceAll("-", "")}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def lukioonValmistavanKoulutuksenOpiskelijaMaaratRaportti(request: RaporttiPäivältäRequest): OppilaitosRaporttiResponse = {
    val oidit = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid).toList
    OppilaitosRaporttiResponse(
      sheets = Seq(LukioonValmistavanKoulutuksenOpiskelijamaaratRaportti.dataSheet(oidit, request.paiva, raportointiDatabase)),
      workbookSettings = WorkbookSettings("", Some(request.password)),
      filename = s"lukioon_valmistavan_koulutuksen_opiskelijamaarat_${request.paiva.toString.replaceAll("-", "")}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def aikuistenPerusopetus(request: AikuistenPerusopetusRaporttiRequest) = {
    OppilaitosRaporttiResponse(
      sheets = AikuistenPerusopetusRaportti(
        aikuistenPerusopetusRepository,
        request.raportinTyyppi,
        request.oppilaitosOid,
        request.alku,
        request.loppu,
        request.osasuoritustenAikarajaus
      ).build(),
      workbookSettings = WorkbookSettings(s"Suoritustietojen_tarkistus_${request.oppilaitosOid}", Some(request.password)),
      filename = s"aikuisten_perusopetus_suoritustietojen_tarkistus_${request.raportinTyyppi.typeName}_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def muuAmmatillinen(request: AikajaksoRaporttiRequest) = OppilaitosRaporttiResponse(
    sheets = Seq(muuammatillinenRaportti.build(request.oppilaitosOid, request.alku, request.loppu)),
    workbookSettings = WorkbookSettings("Muu ammatillinen suoritustietojen tarkistus", Some(request.password)),
    filename = s"muu_ammatillinen_koski_raportti_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-","")}-${request.loppu.toString.replaceAll("-","")}.xlsx",
    downloadToken = request.downloadToken
  )

  def topksAmmatillinen(request: AikajaksoRaporttiRequest) = OppilaitosRaporttiResponse(
    sheets = Seq(topksAmmatillinenRaportti.build(request.oppilaitosOid, request.alku, request.loppu)),
    workbookSettings = WorkbookSettings("TOPKS ammatillinen suoritustietojen tarkistus", Some(request.password)),
    filename = s"topks_ammatillinen_koski_raportti_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-","")}-${request.loppu.toString.replaceAll("-","")}.xlsx",
    downloadToken = request.downloadToken
  )

  def esiopetuksenOppijamäärät(request: RaporttiPäivältäRequest)(implicit u: KoskiSession) = {

    val oppilaitosOids = request.oppilaitosOid match {
      case application.organisaatioService.ostopalveluRootOid =>
        application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)
      case oid => List(oid)
    }

    OppilaitosRaporttiResponse(
      sheets = Seq(esiopetuksenOppijamäärätRaportti.build(oppilaitosOids, request.paiva)),
      workbookSettings = WorkbookSettings("Esiopetuksen oppijamäärien raportti", Some(request.password)),
      filename = s"esiopetuksen_oppijamäärät_raportti-${request.paiva}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def aikuistenperusopetuksenKurssikertymä(request: AikajaksoRaporttiRequest)(implicit u: KoskiSession) = {
    val oppilaitosOids = validateOids(List(request.oppilaitosOid))

    OppilaitosRaporttiResponse(
      sheets = Seq(
        aikuistenPerusopetuksenOppimääränKurssikertymätRaportti.build(oppilaitosOids, request.alku, request.loppu),
        aikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRaportti.build(oppilaitosOids, request.alku, request.loppu),
        aikuistenPerusopetuksenMuutaKauttaRahoitetutKurssitRaportti.build(oppilaitosOids, request.alku, request.loppu),
        aikuistenPerusopetuksenEiRahoitustietoaKurssitRaportti.build(oppilaitosOids, request.alku, request.loppu),
        aikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetRaportti.build(oppilaitosOids, request.alku, request.loppu),
      ),
      workbookSettings = WorkbookSettings("Aikuisten perusopetuksen kurssikertymien raportti", Some(request.password)),
      filename = s"aikuisten_perusopetuksen_kurssikertymät_raportti-${request.alku.toString.replaceAll("-", "")}-${request.loppu.toString.replaceAll("-", "")}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def aikuistenperusopetuksenOppijamäärät(request: RaporttiPäivältäRequest)(implicit u: KoskiSession) = {
    val oppilaitosOids = request.oppilaitosOid match {
      case application.organisaatioService.ostopalveluRootOid =>
        application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)
      case oid =>
        application.organisaatioService.organisaationAlaisetOrganisaatiot(oid)
    }
    OppilaitosRaporttiResponse(
      sheets = Seq(aikuistenPerusopetuksenOppijamäärätRaportti.build(oppilaitosOids, request.paiva)),
      workbookSettings = WorkbookSettings("Aikuisten perusopetuksen oppijamäärien raportti", Some(request.password)),
      filename = s"aikuisten_perusopetuksen_vos_raportti-${request.paiva}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def perusopetuksenOppijamäärät(request: RaporttiPäivältäRequest)(implicit u: KoskiSession) = {
    val oppilaitosOids = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid, "perusopetus")
    OppilaitosRaporttiResponse(
      sheets = Seq(perusopetuksenOppijamäärätRaportti.build(oppilaitosOids, request.paiva)),
      workbookSettings = WorkbookSettings("Perusopetuksen oppijamäärien raportti", Some(request.password)),
      filename = s"perusopetus_vos_raportti-${request.paiva}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def perusopetuksenLisäopetuksenOppijamäärät(request: RaporttiPäivältäRequest)(implicit u: KoskiSession) = {
    val oppilaitosOids = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid)
    OppilaitosRaporttiResponse(
      sheets = Seq(perusopetuksenLisäopetuksenOppijamäärätRaportti.build(oppilaitosOids.toSeq, request.paiva)),
      workbookSettings = WorkbookSettings("Perusopetuksen oppijamäärien raportti", Some(request.password)),
      filename = s"lisaopetus_vos_raportti-${request.paiva}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  private def aikajaksoRaportti(request: AikajaksoRaporttiRequest, raporttiBuilder: AikajaksoRaportti) = {
    val rows = raporttiBuilder.buildRaportti(raportointiDatabase, request.oppilaitosOid, request.alku, request.loppu)
    val documentation = DocumentationSheet("Ohjeet", raporttiBuilder.documentation(request.oppilaitosOid, request.alku, request.loppu, raportointiDatabase.status.completionTime.get.toLocalDateTime))
    val data = DataSheet("Opiskeluoikeudet", rows, raporttiBuilder.columnSettings)

    OppilaitosRaporttiResponse(
      sheets = Seq(data, documentation),
      workbookSettings = WorkbookSettings(raporttiBuilder.title(request.oppilaitosOid, request.alku, request.loppu), Some(request.password)),
      filename = raporttiBuilder.filename(request.oppilaitosOid, request.alku, request.loppu),
      downloadToken = request.downloadToken
    )
  }

  private def perusopetuksenVuosiluokka(request: PerusopetuksenVuosiluokkaRequest, raporttiBuilder: VuosiluokkaRaporttiPaivalta) = {
    val oppilaitosOids = accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid, "perusopetus")
    val rows = raporttiBuilder.buildRaportti(perusopetusRepository, oppilaitosOids, request.paiva, request.vuosiluokka)
    val documentation = DocumentationSheet("Ohjeet", raporttiBuilder.documentation(request.oppilaitosOid, request.paiva, request.vuosiluokka, raportointiDatabase.status.completionTime.get.toLocalDateTime))
    val data = DataSheet("Opiskeluoikeudet", rows, raporttiBuilder.columnSettings)

    OppilaitosRaporttiResponse(
      sheets = Seq(data, documentation),
      workbookSettings = WorkbookSettings(raporttiBuilder.title(request.oppilaitosOid, request.paiva, request.vuosiluokka), Some(request.password)),
      filename = raporttiBuilder.filename(request.oppilaitosOid, request.paiva, request.vuosiluokka),
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
}

case class DynamicResponse(sheets: Seq[Sheet], workbookSettings: WorkbookSettings, filename: String, downloadToken: Option[String])
