package fi.oph.koski.raportit

import java.sql.Date

import fi.oph.koski.config.KoskiApplication

class RaportitService(application: KoskiApplication) {

  private lazy val raportointiDatabase = application.raportointiDatabase
  private lazy val perusopetusRepository = PerusopetuksenRaportitRepository(raportointiDatabase.db)
  private lazy val accessResolver = RaportitAccessResolver(application)
  private lazy val lukioRepository = LukioRaportitRepository(raportointiDatabase.db)
  private lazy val ammatillisenRaportitRepository = AmmatillisenRaportitRepository(raportointiDatabase.db)
  private lazy val muuammatillinenRaportti = MuuAmmatillinenRaporttiBuilder(raportointiDatabase.db)
  private lazy val topksAmmatillinenRaportti = TOPKSAmmatillinenRaporttiBuilder(raportointiDatabase.db)
  private lazy val esiopetusRaportti = EsiopetusRaportti(raportointiDatabase.db)

  def opiskelijaVuositiedot(request: AikajaksoRaporttiRequest): OppilaitosRaporttiResponse = {
    aikajaksoRaportti(request, AmmatillinenOpiskalijavuositiedotRaportti)
  }

  def ammatillinenTutkintoSuoritustietojenTarkistus(request: AmmatillinenSuoritusTiedotRequest): OppilaitosRaporttiResponse = {
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

  def ammatillinenOsittainenSuoritustietojenTarkistus(request: AmmatillinenSuoritusTiedotRequest): OppilaitosRaporttiResponse = {
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

  def lukioraportti(request: AikajaksoRaporttiRequest) = {
    OppilaitosRaporttiResponse(
      sheets = LukioRaportti(lukioRepository).buildRaportti(request.oppilaitosOid, request.alku, request.loppu),
      workbookSettings = WorkbookSettings(s"Suoritustietojen_tarkistus_${request.oppilaitosOid}", Some(request.password)),
      filename = s"lukio_suoritustietojentarkistus_${request.oppilaitosOid}_${request.alku}_${request.loppu}.xlsx",
      downloadToken = request.downloadToken
    )
  }

  def muuAmmatillinen(request: AikajaksoRaporttiRequest) = OppilaitosRaporttiResponse(
    sheets = Seq(muuammatillinenRaportti.build(request.oppilaitosOid, Date.valueOf(request.alku), Date.valueOf(request.loppu))),
    workbookSettings = WorkbookSettings("Muu ammatillinen suoritustietojen tarkistus", Some(request.password)),
    filename = s"muu_ammatillinen_koski_raportti_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-","")}-${request.loppu.toString.replaceAll("-","")}.xlsx",
    downloadToken = request.downloadToken
  )

  def topksAmmatillinen(request: AikajaksoRaporttiRequest) = OppilaitosRaporttiResponse(
    sheets = Seq(topksAmmatillinenRaportti.build(request.oppilaitosOid, Date.valueOf(request.alku), Date.valueOf(request.loppu))),
    workbookSettings = WorkbookSettings("TOPKS ammatillinen suoritustietojen tarkistus", Some(request.password)),
    filename = s"topks_ammatillinen_koski_raportti_${request.oppilaitosOid}_${request.alku.toString.replaceAll("-","")}-${request.loppu.toString.replaceAll("-","")}.xlsx",
    downloadToken = request.downloadToken
  )

  def esiopetus(request: RaporttiPäivältäRequest) = OppilaitosRaporttiResponse(
    sheets = Seq(esiopetusRaportti.build(request.oppilaitosOid, Date.valueOf(request.paiva))),
    workbookSettings = WorkbookSettings("Esiopetus", Some(request.password)),
    filename = s"esiopetus_koski_raportti_${request.oppilaitosOid}_${request.paiva.toString.replaceAll("-","")}.xlsx",
    downloadToken = request.downloadToken
  )

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
    val rows = raporttiBuilder.buildRaportti(perusopetusRepository,  accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid), request.paiva, request.vuosiluokka)
    val documentation = DocumentationSheet("Ohjeet", raporttiBuilder.documentation(request.oppilaitosOid, request.paiva, request.vuosiluokka, raportointiDatabase.status.completionTime.get.toLocalDateTime))
    val data = DataSheet("Opiskeluoikeudet", rows, raporttiBuilder.columnSettings)

    OppilaitosRaporttiResponse(
      sheets = Seq(data, documentation),
      workbookSettings = WorkbookSettings(raporttiBuilder.title(request.oppilaitosOid, request.paiva, request.vuosiluokka), Some(request.password)),
      filename = raporttiBuilder.filename(request.oppilaitosOid, request.paiva, request.vuosiluokka),
      downloadToken = request.downloadToken
    )
  }
}

case class DynamicResponse(sheets: Seq[Sheet], workbookSettings: WorkbookSettings, filename: String, downloadToken: Option[String])
