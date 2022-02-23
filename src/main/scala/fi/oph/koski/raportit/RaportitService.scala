package fi.oph.koski.raportit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.OrganisaatioHierarkia
import fi.oph.koski.raportit.aikuistenperusopetus.{AikuistenPerusopetuksenAineopiskelijoidenKurssikertymät, AikuistenPerusopetuksenEiRahoitustietoaKurssit, AikuistenPerusopetuksenEriVuonnaKorotetutKurssit, AikuistenPerusopetuksenMuutaKauttaRahoitetutKurssit, AikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetKurssit, AikuistenPerusopetuksenOppijamäärätRaportti, AikuistenPerusopetuksenOppimääränKurssikertymät, AikuistenPerusopetusRaportti, AikuistenPerusopetusRaporttiRepository}
import fi.oph.koski.raportit.lukio.{LukioDiaIbInternationalOpiskelijamaaratRaportti, LukioMuutaKauttaRahoitetut, LukioOppiaineEriVuonnaKorotetutKurssit, LukioOppiaineOpiskeluoikeudenUlkopuoliset, LukioOppiaineenOppimaaranKurssikertymat, LukioOppimaaranKussikertymat, LukioRahoitusmuotoEiTiedossa, LukioRaportitRepository, LukioRaportti, LukioonValmistavanKoulutuksenOpiskelijamaaratRaportti}
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid

class RaportitService(application: KoskiApplication) {
  private val raportointiDatabase = application.raportointiDatabase
  private val perusopetusRepository = PerusopetuksenRaportitRepository(raportointiDatabase.db)
  private val accessResolver = RaportitAccessResolver(application)
  private lazy val organisaatioService = application.organisaatioService
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
  private val aikuistenPerusopetuksenEriVuonnaKorotetutKurssitRaportti = AikuistenPerusopetuksenEriVuonnaKorotetutKurssit(raportointiDatabase.db)
  private val perusopetuksenOppijamäärätRaportti = PerusopetuksenOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)
  private val perusopetuksenLisäopetuksenOppijamäärätRaportti = PerusopetuksenLisäopetusOppijamäärätRaportti(raportointiDatabase.db, application.organisaatioService)

  def viimeisinPäivitys = raportointiDatabase.status.startedTime.get.toLocalDateTime

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
        DataSheet("Opiskeluoikeudet", AmmatillinenOsittainenRaportti.buildRaportti(request, ammatillisenRaportitRepository, t), AmmatillinenOsittainenRaportti.columnSettings),
        DocumentationSheet("Ohjeet", AmmatillinenOsittainenRaportti.documentation(request, raportointiDatabase.status.startedTime.get.toLocalDateTime))
      ),
      workbookSettings = WorkbookSettings(AmmatillinenOsittainenRaportti.title(request), Some(request.password)),
      filename = AmmatillinenOsittainenRaportti.filename(request),
      downloadToken = request.downloadToken
    )
  }

  def perusopetuksenVuosiluokka(request: PerusopetuksenVuosiluokkaRequest, t: LocalizationReader): OppilaitosRaporttiResponse = {
    perusopetuksenVuosiluokka(request, PerusopetuksenVuosiluokkaRaportti, t)
  }

  def lukioraportti(request: AikajaksoRaporttiAikarajauksellaRequest, t: LocalizationReader) = {
    OppilaitosRaporttiResponse(
      sheets = LukioRaportti(lukioRepository, t).buildRaportti(request.oppilaitosOid, request.alku, request.loppu, request.osasuoritustenAikarajaus),
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
        LukioOppiaineOpiskeluoikeudenUlkopuoliset.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase),
        LukioOppiaineEriVuonnaKorotetutKurssit.dataSheet(oppilaitosOidit, request.alku, request.loppu, raportointiDatabase)
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
      filename = s"${t.get("raportti-excel-aikuistenperusopetus-tiedoston-etuliite")}_${request.raportinTyyppi.typeName}_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
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

  def esiopetuksenOppijamäärät(request: RaporttiPäivältäRequest, t: LocalizationReader)(implicit u: KoskiSpecificSession) = {

    val oppilaitosOids = request.oppilaitosOid match {
      case application.organisaatioService.ostopalveluRootOid =>
        application.organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)
      case oid => List(oid)
    }

    OppilaitosRaporttiResponse(
      sheets = Seq(esiopetuksenOppijamäärätRaportti.build(oppilaitosOids, request.paiva, t)),
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
      sheets = Seq(perusopetuksenOppijamäärätRaportti.build(oppilaitosOids, request.paiva, t)),
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
            case oid: String => accessResolver.mahdollisetRaporttienTyypitOrganisaatiolle(organisaatio, koulutusmuodot).map(_.toString)
          },
          children = buildOrganisaatioJaRaporttiTyypit(organisaatio.children, koulutusmuodot)
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
