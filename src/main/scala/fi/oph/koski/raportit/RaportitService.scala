package fi.oph.koski.raportit

import fi.oph.koski.config.KoskiApplication

class RaportitService(application: KoskiApplication) {

  private lazy val raportointiDatabase = application.raportointiDatabase
  private lazy val perusopetusRepository = PerusopetuksenRaportitRepository(raportointiDatabase.db)
  private lazy val accessResolver = RaportitAccessResolver(application)
  private lazy val lukioRepository = LukioRaportitRepository(raportointiDatabase.db)

  def opiskelijaVuositiedot(request: AikajaksoRaporttiRequest): OppilaitosRaporttiResponse = {
    aikajaksoRaportti(request, AmmatillinenOpiskalijavuositiedotRaportti)
  }

  def suoritustietojenTarkistus(request: AikajaksoRaporttiRequest): OppilaitosRaporttiResponse = {
    aikajaksoRaportti(request, AmmatillinenTutkintoRaportti)
  }

  def ammatillinenOsittainenSuoritustietojenTarkistus(request: AikajaksoRaporttiRequest): OppilaitosRaporttiResponse = {
    aikajaksoRaportti(request, AmmatillinenOsittainenRaportti)
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
