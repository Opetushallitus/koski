package fi.oph.koski.raportit

import fi.oph.koski.config.KoskiApplication

class RaportitService(application: KoskiApplication) {

  private lazy val raportointiDatabase = application.raportointiDatabase
  private lazy val perusopetusRepository = PerusopetuksenRaportitRepository(raportointiDatabase.db)
  private lazy val accessResolver = RaportitAccessResolver(application)

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

  private def aikajaksoRaportti(request: AikajaksoRaporttiRequest, raporttiBuilder: AikajaksoRaportti) = {
    val rows = raporttiBuilder.buildRaportti(raportointiDatabase, request.oppilaitosOid, request.alku, request.loppu)
    val documentation = DocumentationSheet("Ohjeet", raporttiBuilder.documentation(request.oppilaitosOid, request.alku, request.loppu, raportointiDatabase.fullLoadCompleted(raportointiDatabase.statuses).get))
    val data = DataSheet("Opiskeluoikeudet", rows, raporttiBuilder.columnSettings)

    OppilaitosRaporttiResponse(
      rows = rows,
      sheets = Seq(data, documentation),
      workbookSettings = WorkbookSettings(raporttiBuilder.title(request.oppilaitosOid, request.alku, request.loppu), Some(request.password)),
      filename = raporttiBuilder.filename(request.oppilaitosOid, request.alku, request.loppu),
      downloadToken = request.downloadToken
    )
  }

  private def perusopetuksenVuosiluokka(request: PerusopetuksenVuosiluokkaRequest, raporttiBuilder: VuosiluokkaRaporttiPaivalta) = {
    val rows = raporttiBuilder.buildRaportti(perusopetusRepository,  accessResolver.kyselyOiditOrganisaatiolle(request.oppilaitosOid), request.paiva, request.vuosiluokka)
    val documentation = DocumentationSheet("Ohjeet", raporttiBuilder.documentation(request.oppilaitosOid, request.paiva, request.vuosiluokka, raportointiDatabase.fullLoadCompleted(raportointiDatabase.statuses).get))
    val data = DataSheet("Opiskeluoikeudet", rows, raporttiBuilder.columnSettings)

    OppilaitosRaporttiResponse(
      rows = rows,
      sheets = Seq(data, documentation),
      workbookSettings = WorkbookSettings(raporttiBuilder.title(request.oppilaitosOid, request.paiva, request.vuosiluokka), Some(request.password)),
      filename = raporttiBuilder.filename(request.oppilaitosOid, request.paiva, request.vuosiluokka),
      downloadToken = request.downloadToken
    )
  }
}
